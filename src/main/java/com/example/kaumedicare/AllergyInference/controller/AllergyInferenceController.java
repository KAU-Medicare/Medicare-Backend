package com.example.kaumedicare.AllergyInference.controller;

import com.example.kaumedicare.AllergyInference.service.AllergyInferenceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/allergy")
@Tag(name = "Allergy-Inference-API", description = "알레르기 추론 API")
public class AllergyInferenceController {
    private final AllergyInferenceService allergyInferenceService;

    public AllergyInferenceController(AllergyInferenceService allergyInferenceService) {
        this.allergyInferenceService = allergyInferenceService;
    }

    @PostMapping("/analyze")
    public Mono<ResponseEntity<Map>> analyzeAllergy(
            @RequestParam String kakaoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate occurredDate,
            @RequestBody Map<String, Object> requestBody) {
        return allergyInferenceService.analyzeAllergy(kakaoId, occurredDate, requestBody)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/result")
    public ResponseEntity<Map> getAllergyAnalysis(
            @RequestParam String kakaoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate occurredDate) {
        Map result = allergyInferenceService.getAnalysisResult(kakaoId, occurredDate);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAnalysis(
            @RequestParam String kakaoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate occurredDate) {
        allergyInferenceService.deleteAnalysis(kakaoId, occurredDate);
        return ResponseEntity.noContent().build();
    }
}