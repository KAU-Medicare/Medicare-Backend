package com.example.kaumedicare.AllergyInference.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class AllergyInferenceService {

    private final WebClient webClient;

    public AllergyInferenceService(@Value("${allergyinference.server.url}") String allergyInferenceUrl) {
        this.webClient = WebClient.create(allergyInferenceUrl);
    }

    public Mono<Map> analyzeAllergy(Map<String, Object> requestBody) {
        return webClient.post()
                .uri("/analyze_allergy")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class);
    }
}