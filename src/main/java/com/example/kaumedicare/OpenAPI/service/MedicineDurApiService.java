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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineDurApiService {

    private static final String DUR_BASE_URL = "https://apis.data.go.kr/1471000/DURPrdlstInfoService03/getUsjntTabooInfoList03";
    private static final int TOTAL_COUNT = 309747; // DUR API의 전체 데이터 수
    private static final int NUM_OF_ROWS = 100;

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
                String urlBuilder = DUR_BASE_URL + "?" + URLEncoder.encode("serviceKey", StandardCharsets.UTF_8) + "=" + serviceKey +
                        "&" + URLEncoder.encode("pageNo", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(String.valueOf(pageNo), StandardCharsets.UTF_8) +
                        "&" + URLEncoder.encode("numOfRows", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(String.valueOf(NUM_OF_ROWS), StandardCharsets.UTF_8) +
                        "&" + URLEncoder.encode("type", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("json", StandardCharsets.UTF_8);

                URL url = new URL(urlBuilder);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-type", "application/json");

                log.info("DUR API 페이지 {} 처리 중 ({}/{})", pageNo, pageNo, totalPages);

                BufferedReader rd;
                if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                    rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    String errorResponse = rd.readLine();
                    log.error("DUR API 페이지 {} 에러 응답: {}", pageNo, errorResponse);
                    rd.close();
                    conn.disconnect();
                    continue;
                }

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }

                rd.close();
                conn.disconnect();

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(sb.toString());
                JsonNode items = rootNode.path("body").path("items");

                if (items.isEmpty() || items.isNull()) {
                    log.warn("DUR API 페이지 {}: 데이터가 없습니다.", pageNo);
                    continue;
                }

                int pageSuccessCount = 0;
                for (JsonNode item : items) {
                    // 원래 약품 저장
                    String mainItemSeq = item.path("ITEM_SEQ").asText();
                    if (!processedItemSeqs.contains(mainItemSeq) && !medicineRepository.existsByItemSeq(mainItemSeq)) {
                        saveMedicine(medicineRepository, mainItemSeq,
                                item.path("ITEM_NAME").asText(),
                                item.path("ENTP_NAME").asText());
                        processedItemSeqs.add(mainItemSeq);
                        pageSuccessCount++;
                        successCount++;
                    } else {
                        skipCount++;
                    }

                    // 병용금기 약품 저장
                    String mixtureItemSeq = item.path("MIXTURE_ITEM_SEQ").asText();
                    if (!processedItemSeqs.contains(mixtureItemSeq) && !medicineRepository.existsByItemSeq(mixtureItemSeq)) {
                        saveMedicine(medicineRepository, mixtureItemSeq,
                                item.path("MIXTURE_ITEM_NAME").asText(),
                                item.path("MIXTURE_ENTP_NAME").asText());
                        processedItemSeqs.add(mixtureItemSeq);
                        pageSuccessCount++;
                        successCount++;
                    } else {
                        skipCount++;
                    }
                }

                log.info("DUR API 페이지 {} 완료: {}개 저장, {}개 스킵 (총 저장: {})",
                        pageNo, pageSuccessCount, skipCount, successCount);

                Thread.sleep(500); // API 호출 간격 조절
            }

            log.info("DUR API 작업 완료: 총 {}개 저장, {}개 스킵", successCount, skipCount);

        } catch (Exception e) {
            log.error("DUR API 의약품 데이터 가져오기 실패 (페이지 {}): ", pageNo, e);
            throw new RuntimeException("DUR API 의약품 데이터 가져오기 실패: " + e.getMessage());
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