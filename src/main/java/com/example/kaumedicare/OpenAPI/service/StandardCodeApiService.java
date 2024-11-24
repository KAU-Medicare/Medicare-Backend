package com.example.kaumedicare.OpenAPI.service;

import com.example.kaumedicare.StandardCode.model.StandardCode;
import com.example.kaumedicare.StandardCode.repository.StandardCodeRepository;
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
public class StandardCodeApiService {

    private static final String BASE_URL = "http://apis.data.go.kr/B551182/medicInsupPriceInfoService/getMedicInsupPriceInfo";

    @Value("${api.service-key}")
    private String serviceKey;

    private final ObjectMapper objectMapper;
    private final StandardCodeRepository standardCodeRepository;

    public void fetchAndSaveAllStandardCodes() {
        log.info("의약품 표준코드 데이터 업데이트 시작");
        try {
            List<StandardCode> codes = fetchAllStandardCodes();
            standardCodeRepository.deleteAll();
            standardCodeRepository.saveAll(codes);
            log.info("의약품 표준코드 데이터 업데이트 완료: {} 건", codes.size());
        } catch (Exception e) {
            log.error("의약품 표준코드 데이터 업데이트 실패", e);
            throw new RuntimeException("의약품 표준코드 데이터 업데이트 실패", e);
        }
    }

    private List<StandardCode> fetchAllStandardCodes() {
        List<StandardCode> results = new ArrayList<>();
        int pageNo = 1;
        int numOfRows = 100;
        boolean hasMore = true;

        try {
            while (hasMore) {
                String urlBuilder = BASE_URL + "?" +
                        URLEncoder.encode("serviceKey", StandardCharsets.UTF_8) + "=" + serviceKey +
                        "&" + URLEncoder.encode("pageNo", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(String.valueOf(pageNo), StandardCharsets.UTF_8) +
                        "&" + URLEncoder.encode("numOfRows", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(String.valueOf(numOfRows), StandardCharsets.UTF_8) +
                        "&" + URLEncoder.encode("type", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("json", StandardCharsets.UTF_8);

                URL url = new URL(urlBuilder);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-type", "application/json");

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

                JsonNode rootNode = objectMapper.readTree(sb.toString());
                JsonNode items = rootNode.path("body").path("items");

                if (items.isEmpty() || items.isNull()) {
                    hasMore = false;
                    continue;
                }

                for (JsonNode item : items) {
                    StandardCode code = StandardCode.builder()
                            .standardCode(item.path("edi_code").asText())
                            .itemSeq(item.path("itemSeq").asText())
                            .build();
                    results.add(code);
                }

                pageNo++;
                Thread.sleep(500);  // API 호출 간격 조절
            }
        } catch (Exception e) {
            log.error("페이지 {} 데이터 가져오기 실패", pageNo, e);
        }
        return results;
    }
}