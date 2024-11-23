package com.example.kaumedicare.Chatbotapp.controller;

import com.example.kaumedicare.Chatbotapp.dto.ChatbotRequest;
import com.example.kaumedicare.Chatbotapp.dto.ChatbotResponse;
import com.example.kaumedicare.Chatbotapp.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
@Tag(name = "Chatbot", description = "AI 챗봇 API")
public class ChatbotController {
    private final ChatbotService chatbotService;

    @PostMapping
    @Operation(summary = "챗봇과 대화", description = "사용자 메시지를 전송하고 AI 챗봇의 응답을 받습니다.")
    @ApiResponse(responseCode = "200", description = "성공적으로 응답을 받음")
    public ResponseEntity<ChatbotResponse> chat(@RequestBody ChatbotRequest request) {
        try {
            String response = chatbotService.getChatbotResponse(request.getMessage());
            return ResponseEntity.ok(new ChatbotResponse(response));
        } catch (Exception e) {
            log.error("챗봇 요청 처리 중 오류 발생: ", e);
            return ResponseEntity.internalServerError()
                    .body(new ChatbotResponse("서버 내부 오류가 발생했습니다."));
        }
    }
}