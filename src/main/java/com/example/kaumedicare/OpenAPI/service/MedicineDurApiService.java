package com.example.kaumedicare.OpenAPI.service;

import com.example.kaumedicare.Medicine.model.Medicine;
import com.example.kaumedicare.Medicine.repository.MedicineRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineDurApiService {
    private static final String DUR_BASE_URL = "https://apis.data.go.kr/1471000/DURPrdlstInfoService03/getUsjntTabooInfoList03";
    private static final int TOTAL_COUNT = 309747;
    private static final int NUM_OF_ROWS = 100;
    private static final int MAX_RETRIES = 15;
    private static final long RETRY_DELAY_MS = 1000;
    private static final long RATE_LIMIT_DELAY_MS = 500;

    @Value("${api.service-key}")
    private String serviceKey;

    public void fetchAndSaveAllDurMedicines(MedicineRepository medicineRepository) {
        int pageNo = 1;
        int totalPages = (int) Math.ceil((double) TOTAL_COUNT / NUM_OF_ROWS);
        Set<String> processedItemSeqs = new HashSet<>();
        int successCount = 0;
        int skipCount = 0;

        try {
            for (pageNo = 1; pageNo <= totalPages; pageNo++) {
                final int currentPage = pageNo;
                JsonNode items = retryWithBackoff(() -> {
                    try {
                        return fetchPageData(currentPage);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                if (items == null || items.isEmpty() || items.isNull()) {
                    log.warn("DUR API 페이지 {}: 데이터가 없습니다.", currentPage);
                    continue;
                }

                int pageSuccessCount = 0;
                int pageSkipCount = 0;

                for (JsonNode item : items) {
                    // 원래 약품 저장
                    String mainItemSeq = item.path("ITEM_SEQ").asText();
                    if (!processedItemSeqs.contains(mainItemSeq) && !medicineRepository.existsByItemSeq(mainItemSeq)) {
                        saveMedicine(medicineRepository, mainItemSeq,
                                item.path("ITEM_NAME").asText(),
                                item.path("ENTP_NAME").asText());
                        processedItemSeqs.add(mainItemSeq);
                        pageSuccessCount++;
                    } else {
                        pageSkipCount++;
                    }

                    // 병용금기 약품 저장
                    String mixtureItemSeq = item.path("MIXTURE_ITEM_SEQ").asText();
                    if (!processedItemSeqs.contains(mixtureItemSeq) && !medicineRepository.existsByItemSeq(mixtureItemSeq)) {
                        saveMedicine(medicineRepository, mixtureItemSeq,
                                item.path("MIXTURE_ITEM_NAME").asText(),
                                item.path("MIXTURE_ENTP_NAME").asText());
                        processedItemSeqs.add(mixtureItemSeq);
                        pageSuccessCount++;
                    } else {
                        pageSkipCount++;
                    }
                }

                successCount += pageSuccessCount;
                skipCount += pageSkipCount;

                log.info("DUR API 페이지 {} 완료: {}개 저장, {}개 스킵 (총 저장: {})",
                        currentPage, pageSuccessCount, pageSkipCount, successCount);

                Thread.sleep(RATE_LIMIT_DELAY_MS);
            }

            log.info("DUR API 작업 완료: 총 {}개 저장, {}개 스킵", successCount, skipCount);

        } catch (Exception e) {
            log.error("DUR API 의약품 데이터 가져오기 실패 (페이지 {}): ", pageNo, e);
            throw new RuntimeException("DUR API 의약품 데이터 가져오기 실패: " + e.getMessage());
        }
    }

    private JsonNode fetchPageData(int pageNo) throws Exception {
        String urlBuilder = buildUrl(pageNo);
        URL url = new URL(urlBuilder);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        try {
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = readResponse(conn.getInputStream());
                validateJsonResponse(response);
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readTree(response).path("body").path("items");
            } else {
                String errorResponse = readResponse(conn.getErrorStream());
                log.error("DUR API 오류 응답 (페이지 {}): {}", pageNo, errorResponse);
                throw new RuntimeException("API 호출 실패: " + conn.getResponseCode());
            }
        } finally {
            conn.disconnect();
        }
    }

    private void validateJsonResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            throw new RuntimeException("빈 응답이 반환되었습니다.");
        }
        if (response.trim().startsWith("<")) {
            throw new RuntimeException("API가 HTML을 반환했습니다. 유효한 JSON이 아닙니다.");
        }
    }

    private <T> T retryWithBackoff(Supplier<T> operation) throws Exception {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < MAX_RETRIES) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                attempts++;
                if (attempts < MAX_RETRIES) {
                    long delay = RETRY_DELAY_MS * (long) Math.pow(2, attempts - 1);
                    log.warn("재시도 {} / {}, {}ms 후 다시 시도합니다. 오류: {}",
                            attempts, MAX_RETRIES, delay, e.getMessage());
                    Thread.sleep(delay);
                }
            }
        }
        throw new RuntimeException("최대 재시도 횟수 초과", lastException);
    }

    private String buildUrl(int pageNo) throws Exception {
        return DUR_BASE_URL + "?" +
                URLEncoder.encode("serviceKey", StandardCharsets.UTF_8) + "=" + serviceKey +
                "&" + URLEncoder.encode("pageNo", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(String.valueOf(pageNo), StandardCharsets.UTF_8) +
                "&" + URLEncoder.encode("numOfRows", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(String.valueOf(NUM_OF_ROWS), StandardCharsets.UTF_8) +
                "&" + URLEncoder.encode("type", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("json", StandardCharsets.UTF_8);
    }

    private String readResponse(InputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private void saveMedicine(MedicineRepository medicineRepository, String itemSeq, String itemName, String entpName) {
        try {
            if (itemSeq != null && !itemSeq.isEmpty()) {
                String processedItemName = itemName != null ?
                        (itemName.length() > 1000 ? itemName.substring(0, 1000) : itemName) : "";

                Medicine medicine = Medicine.builder()
                        .itemSeq(itemSeq)
                        .itemName(processedItemName)
                        .entpName(entpName)
                        .build();

                medicineRepository.save(medicine);
            }
        } catch (Exception e) {
            log.warn("약품 저장 실패 (itemSeq: {}): {}", itemSeq, e.getMessage());
        }
    }
}