package com.example.kaumedicare.Chatbotapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ChatbotService {
    @Value("${chatbot.server.url}")
    private String chatbotServerUrl;

    public String getChatbotResponse(String message) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 요청 바디 생성
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("message", message);

            // HTTP 엔티티 생성
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            // API 호출
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    chatbotServerUrl,
                    entity,
                    Map.class
            );

            // 응답에서 response 필드 추출
            Map<String, String> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("response")) {
                return responseBody.get("response");
            }

            throw new RuntimeException("Invalid response format from AI service");
        } catch (Exception e) {
            log.error("AI 서비스 호출 중 오류 발생: ", e);
            throw new RuntimeException("AI 서비스 연결 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
