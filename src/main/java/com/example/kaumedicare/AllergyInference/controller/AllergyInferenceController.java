package com.example.kaumedicare.AllergyInference.controller;

import com.example.kaumedicare.AllergyInference.service.AllergyInferenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/allergy")
public class AllergyInferenceController {

    private final AllergyInferenceService allergyInferenceService;

    public AllergyInferenceController(AllergyInferenceService allergyInferenceService) {
        this.allergyInferenceService = allergyInferenceService;
    }

    @PostMapping("/analyze")
    public Mono<ResponseEntity<Map>> analyzeAllergy(@RequestBody Map<String, Object> requestBody) {
        return allergyInferenceService.analyzeAllergy(requestBody)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}