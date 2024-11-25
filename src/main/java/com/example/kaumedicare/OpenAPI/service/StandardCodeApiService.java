package com.example.kaumedicare.OpenAPI.service;

import com.example.kaumedicare.StandardCode.model.StandardCode;
import com.example.kaumedicare.StandardCode.repository.StandardCodeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class StandardCodeApiService {

    private static final String BASE_URL = "https://api.odcloud.kr/api/15067462/v1/uddi:e97e9cfb-4d88-4d67-8983-a6b63fa130b5";
    private static final int MAX_RETRY_ATTEMPTS = 10;
    private static final int RETRY_DELAY_MS = 1000;
    private static final int BATCH_SIZE = 1000;
    private final ObjectMapper objectMapper;
    private final StandardCodeRepository standardCodeRepository;
    @Value("${api.service-key}")
    private String serviceKey;

    @Async
    public CompletableFuture<Void> fetchAndSaveAllStandardCodes() {
        return CompletableFuture.runAsync(() -> {
            log.info("의약품 표준코드 데이터 업데이트 시작");
            int pageNo = 1;
            int numOfRows = 100;
            int successCount = 0;
            int skipCount = 0;
            List<StandardCode> allCodes = new ArrayList<>();

            try {
                while (true) {
                    JsonNode response = fetchPageWithRetry(pageNo, numOfRows);
                    if (response == null) {
                        break;
                    }

                    JsonNode items = response.path("data");
                    int totalCount = response.path("totalCount").asInt();

                    if (items.isEmpty() || items.size() == 0) {
                        log.info("더 이상 데이터가 없습니다.");
                        break;
                    }

                    List<StandardCode> pageData = new ArrayList<>();
                    for (JsonNode item : items) {
                        String standardCode = item.path("표준코드").asText();

                        if (!standardCodeRepository.existsByStandardCode(standardCode)) {
                            StandardCode code = StandardCode.builder()
                                    .standardCode(standardCode)
                                    .itemSeq(item.path("품목기준코드").asText())
                                    .build();
                            pageData.add(code);
                        } else {
                            skipCount++;
                        }
                    }

                    if (!pageData.isEmpty()) {
                        allCodes.addAll(pageData);
                        successCount += pageData.size();
                        log.info("페이지 {} 완료: {}개 수집, {}개 스킵 (총 수집 {}개)",
                                pageNo, pageData.size(), skipCount, successCount);
                    }

                    if (pageNo * numOfRows >= totalCount) {
                        log.info("모든 데이터 수집 완료");
                        break;
                    }

                    pageNo++;
                    Thread.sleep(500); // API 호출 간격
                }

                // 배치 처리로 저장
                if (!allCodes.isEmpty()) {
                    saveBatch(allCodes);
                }

                log.info("작업 완료: 총 {}개 저장, {}개 스킵", successCount, skipCount);

            } catch (Exception e) {
                log.error("의약품 표준코드 데이터 처리 중 오류 발생", e);
                throw new RuntimeException("데이터 처리 실패", e);
            }
        });
    }

    @Transactional
    protected void saveBatch(List<StandardCode> codes) {
        try {
            log.info("데이터베이스 저장 시작: 총 {}건", codes.size());
            standardCodeRepository.deleteAll();
            log.info("기존 데이터 삭제 완료");

            for (int i = 0; i < codes.size(); i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, codes.size());
                List<StandardCode> batch = codes.subList(i, end);
                standardCodeRepository.saveAll(batch);
                log.info("배치 저장 진행: {}/{} 완료", end, codes.size());
            }
            log.info("데이터베이스 저장 완료");
        } catch (Exception e) {
            log.error("데이터 저장 중 오류 발생", e);
            throw new RuntimeException("데이터 저장 실패", e);
        }
    }

    private JsonNode fetchPageWithRetry(int pageNo, int numOfRows) {
        int attempts = 0;
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                String urlBuilder = BASE_URL + "?" +
                        URLEncoder.encode("serviceKey", StandardCharsets.UTF_8) + "=" + serviceKey +
                        "&" + URLEncoder.encode("page", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(String.valueOf(pageNo), StandardCharsets.UTF_8) +
                        "&" + URLEncoder.encode("perPage", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(String.valueOf(numOfRows), StandardCharsets.UTF_8) +
                        "&" + URLEncoder.encode("type", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("json", StandardCharsets.UTF_8);

                URL url = new URL(urlBuilder);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-type", "application/json");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                try {
                    int responseCode = conn.getResponseCode();
                    if (responseCode >= 200 && responseCode <= 300) {
                        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = rd.readLine()) != null) {
                            sb.append(line);
                        }
                        rd.close();
                        return objectMapper.readTree(sb.toString());
                    } else {
                        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                        String errorResponse = rd.readLine();
                        rd.close();
                        log.warn("페이지 {} 요청 실패 (시도 {}/{}): {} (응답 코드: {})",
                                pageNo, attempts + 1, MAX_RETRY_ATTEMPTS, errorResponse, responseCode);
                    }
                } finally {
                    conn.disconnect();
                }

                attempts++;
                if (attempts < MAX_RETRY_ATTEMPTS) {
                    Thread.sleep((long) RETRY_DELAY_MS * attempts); // 지수 백오프
                }
            } catch (Exception e) {
                attempts++;
                log.warn("페이지 {} 처리 중 오류 발생 (시도 {}/{}): {}",
                        pageNo, attempts, MAX_RETRY_ATTEMPTS, e.getMessage());
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    log.error("최대 재시도 횟수 초과", e);
                    break;
                }
                try {
                    Thread.sleep((long) RETRY_DELAY_MS * attempts);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return null;
    }
}
