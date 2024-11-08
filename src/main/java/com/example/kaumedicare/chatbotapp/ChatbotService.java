package com.example.chatbotapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ChatbotService {

    @Value("${chatbot.server.url}")
    private String chatbotServerUrl;

    public String getChatbotResponse(String message) {
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.postForObject(chatbotServerUrl, Map.of("message", message), String.class);
        return response;
    }
}