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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineApiService {

    private static final String BASE_URL = "http://apis.data.go.kr/1471000/DrbEasyDrugInfoService/getDrbEasyDrugList";
    @Value("${api.service-key}")
    private String serviceKey;

    public void fetchAndSaveAllMedicines(MedicineRepository medicineRepository) {
        int pageNo = 1;
        int numOfRows = 100;
        int totalCount = 4779;
        int totalPages = (int) Math.ceil((double) totalCount / numOfRows);

        int successCount = 0;
        int skipCount = 0;

        try {
            for (pageNo = 1; pageNo <= totalPages; pageNo++) {
                String urlBuilder = BASE_URL + "?" + URLEncoder.encode("serviceKey", StandardCharsets.UTF_8) + "=" + serviceKey +
                        "&" + URLEncoder.encode("pageNo", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(String.valueOf(pageNo), StandardCharsets.UTF_8) +
                        "&" + URLEncoder.encode("numOfRows", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(String.valueOf(numOfRows), StandardCharsets.UTF_8) +
                        "&" + URLEncoder.encode("type", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("json", StandardCharsets.UTF_8);

                URL url = new URL(urlBuilder);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-type", "application/json");

                log.info("페이지 {} 처리 중 ({}/{})", pageNo, pageNo, totalPages);

                BufferedReader rd;
                if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                    rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    String errorResponse = rd.readLine();
                    log.error("페이지 {} API 에러 응답: {}", pageNo, errorResponse);
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

                String responseBody = sb.toString();

                if (responseBody.contains("SERVICE_KEY_IS_NOT_REGISTERED_ERROR")) {
                    throw new RuntimeException("API 키가 등록되지 않았습니다.");
                }

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode items = rootNode.path("body").path("items");

                if (items.isEmpty() || items.isNull()) {
                    log.warn("페이지 {}: 데이터가 없습니다.", pageNo);
                    continue;
                }

                List<Medicine> medicines = new ArrayList<>();
                for (JsonNode item : items) {
                    String itemSeq = item.path("itemSeq").asText();

                    // itemSeq로 중복 체크
                    if (!medicineRepository.existsByItemSeq(itemSeq)) {
                        String itemName = item.path("itemName").asText();
                        if (itemName.length() > 1000) {
                            log.warn("아이템 이름이 너무 깁니다. 길이: {}, itemSeq: {}",
                                    itemName.length(), itemSeq);
                            itemName = itemName.substring(0, 1000);
                        }

                        Medicine medicine = Medicine.builder()
                                .entpName(item.path("entpName").asText())
                                .itemName(itemName)
                                .itemSeq(itemSeq)
                                .build();
                        medicines.add(medicine);
                    } else {
                        skipCount++;
                        log.debug("중복 데이터 스킵: itemSeq={}", itemSeq);
                    }
                }

                if (!medicines.isEmpty()) {
                    medicineRepository.saveAll(medicines);
                    successCount += medicines.size();
                    log.info("페이지 {} 완료: {}개 저장, {}개 스킵 (총 저장 {}/{}개)",
                            pageNo, medicines.size(), skipCount, successCount, totalCount);
                } else {
                    log.info("페이지 {}: 모든 데이터가 이미 존재함", pageNo);
                }

                Thread.sleep(500);
            }

            log.info("작업 완료: 총 {}개 저장, {}개 스킵", successCount, skipCount);

        } catch (Exception e) {
            log.error("의약품 데이터 가져오기 실패 (페이지 {}): ", pageNo, e);
            throw new RuntimeException("의약품 데이터 가져오기 실패: " + e.getMessage());
        }
    }
}
