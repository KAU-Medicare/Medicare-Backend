package com.example.kaumedicare.Chatbotapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatbotResponse {
    @Schema(description = "AI 챗봇 응답")
    private String response;
}