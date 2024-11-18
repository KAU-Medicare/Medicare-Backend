package com.example.kaumedicare.Chatbotapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatbotRequest {
    @Schema(description = "사용자 메시지", example = "비타민D 복용시 주의사항이 뭔가요?")
    private String message;
}