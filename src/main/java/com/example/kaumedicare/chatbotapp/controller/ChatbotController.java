package com.example.kaumedicare.Chatbotapp.controller;

import com.example.kaumedicare.Chatbotapp.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatbotController {
    private final ChatbotService chatbotService;

    @PostMapping
    public Map<String, String> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String response = chatbotService.getChatbotResponse(message);
        return Map.of("response", response);
    }
}