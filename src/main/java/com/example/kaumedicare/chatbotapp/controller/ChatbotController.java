package com.example.kaumedicare.Chatbotapp.controller;

import com.example.kaumedicare.Chatbotapp.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatbotController {
    private final ChatbotService chatbotService;

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody Map<String, String> request) {
        try {
            if (!request.containsKey("message")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "메시지가 필요합니다."));
            }

            String message = request.get("message");
            String response = chatbotService.getChatbotResponse(message);
            return ResponseEntity.ok(Map.of("response", response));
        } catch (Exception e) {
            log.error("챗봇 요청 처리 중 오류 발생: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "서버 내부 오류가 발생했습니다."));
        }
    }
}
//