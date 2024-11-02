package com.example.kaumedicare.OpenAPI.service;

import com.example.kaumedicare.Dur.model.Dur;
import com.example.kaumedicare.Dur.repository.DurRepository;
import com.example.kaumedicare.Medicine.model.Medicine;
import com.example.kaumedicare.Medicine.repository.MedicineRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DurApiService {

    private static final String DUR_BASE_URL = "https://apis.data.go.kr/1471000/DURPrdlstInfoService03/getUsjntTabooInfoList03";
    private static final int BATCH_SIZE = 500;
    private static final int MAX_RETRIES = 100;
    private final MedicineRepository medicineRepository;
    private final DurRepository durRepository;
    @Value("${api.service-key}")
    private String serviceKey;

    public void fetchAndSaveDurRelations() {
        int pageNo = 1;
        int numOfRows = 100;
        Set<String> processedPairs = new HashSet<>();
        List<Dur> durBatch = new ArrayList<>();
        int totalSaved = 0;
        int totalSkipped = 0;

        try {
            // 먼저 모든 Medicine의 itemSeq와 id를 메모리에 캐시
            Map<String, Medicine> medicineCache = loadMedicineCache();
            log.info("Medicine 캐시 로드 완료. 총 {} 개의 약품", medicineCache.size());

            while (true) {
                Thread.sleep(500);

                // API 호출 및 재시도 로직
                String response = getResponseWithRetry(pageNo, numOfRows);
                if (response == null) {
                    log.warn("페이지 {} 처리 실패, 다음 페이지로 진행", pageNo);
                    pageNo++;
                    continue;
                }

                JsonNode rootNode = new ObjectMapper().readTree(response);
                JsonNode items = rootNode.path("body").path("items");

                if (items.isEmpty()) {
                    log.info("더 이상 데이터가 없습니다. 페이지 {}", pageNo);
                    break;
                }

                int pageSaved = 0;
                int pageSkipped = 0;

                for (JsonNode item : items) {
                    String itemSeq = item.path("ITEM_SEQ").asText();
                    String mixtureItemSeq = item.path("MIXTURE_ITEM_SEQ").asText();

                    // 중복 체크
                    String pairKey = itemSeq + ":" + mixtureItemSeq;
                    if (processedPairs.contains(pairKey)) {
                        pageSkipped++;
                        continue;
                    }

                    Medicine targetMedicine = medicineCache.get(itemSeq);
                    Medicine durMedicine = medicineCache.get(mixtureItemSeq);

                    if (targetMedicine != null && durMedicine != null) {
                        durBatch.add(new Dur(targetMedicine, durMedicine));
                        processedPairs.add(pairKey);
                        pageSaved++;

                        // 배치 크기에 도달하면 저장
                        if (durBatch.size() >= BATCH_SIZE) {
                            saveDurBatch(durBatch);
                            durBatch.clear();
                        }
                    }
                }

                totalSaved += pageSaved;
                totalSkipped += pageSkipped;
                log.info("페이지 {} 처리 완료 - 저장: {}, 스킵: {}, 총 저장: {}, 총 스킵: {}",
                        pageNo, pageSaved, pageSkipped, totalSaved, totalSkipped);

                pageNo++;
            }

            // 남은 배치 처리
            if (!durBatch.isEmpty()) {
                saveDurBatch(durBatch);
                log.info("최종 배치 처리 완료 - 총 저장: {}, 총 스킵: {}", totalSaved, totalSkipped);
            }

        } catch (Exception e) {
            log.error("병용금기 데이터 가져오기 실패: ", e);
            throw new RuntimeException("병용금기 데이터 가져오기 실패: " + e.getMessage(), e);
        }
    }

    private Map<String, Medicine> loadMedicineCache() {
        List<Medicine> allMedicines = medicineRepository.findAll();
        return allMedicines.stream()
                .collect(Collectors.toMap(
                        Medicine::getItemSeq,
                        medicine -> medicine,
                        (existing, replacement) -> existing
                ));
    }

    @Transactional
    protected void saveDurBatch(List<Dur> durBatch) {
        try {
            durRepository.saveAll(durBatch);
        } catch (Exception e) {
            log.error("배치 저장 중 오류 발생: {}", e.getMessage());
        }
    }

    private String getResponseWithRetry(int pageNo, int numOfRows) {
        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                String response = getApiResponse(buildUrl(pageNo, numOfRows));
                if (response != null && !response.trim().startsWith("<")) {
                    return response;
                }
                log.warn("페이지 {} 재시도 {}/{}", pageNo, retryCount + 1, MAX_RETRIES);
                Thread.sleep(2000);
                retryCount++;
            } catch (Exception e) {
                log.warn("API 호출 실패 (재시도 {}/{}): {}", retryCount + 1, MAX_RETRIES, e.getMessage());
                retryCount++;
            }
        }
        return null;
    }

    private String buildUrl(int pageNo, int numOfRows) throws Exception {
        String urlBuilder = DUR_BASE_URL + "?" + URLEncoder.encode("serviceKey", StandardCharsets.UTF_8) + "=" + serviceKey +
                "&" + URLEncoder.encode("pageNo", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(String.valueOf(pageNo), StandardCharsets.UTF_8) +
                "&" + URLEncoder.encode("numOfRows", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(String.valueOf(numOfRows), StandardCharsets.UTF_8) +
                "&" + URLEncoder.encode("type", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("json", StandardCharsets.UTF_8);
        return urlBuilder;
    }

    private String getApiResponse(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json");

            try (BufferedReader rd = new BufferedReader(
                    new InputStreamReader(
                            conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300 ?
                                    conn.getInputStream() : conn.getErrorStream()))) {

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            log.error("API 호출 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }
}