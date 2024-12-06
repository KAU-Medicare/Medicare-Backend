package com.example.kaumedicare.AllergyInference.service;

import com.example.kaumedicare.AllergyInference.model.AllergyAnalysis;
import com.example.kaumedicare.AllergyInference.model.AnalysisReason;
import com.example.kaumedicare.AllergyInference.model.SuspectedMedication;
import com.example.kaumedicare.AllergyInference.repository.AllergyAnalysisRepository;
import com.example.kaumedicare.User.model.User;
import com.example.kaumedicare.User.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AllergyInferenceService {
    private final WebClient webClient;
    private final AllergyAnalysisRepository allergyAnalysisRepository;
    private final UserRepository userRepository;

    public AllergyInferenceService(
            @Value("${allergyinference.server.url}") String allergyInferenceUrl,
            AllergyAnalysisRepository allergyAnalysisRepository,
            UserRepository userRepository) {
        this.webClient = WebClient.create(allergyInferenceUrl);
        this.allergyAnalysisRepository = allergyAnalysisRepository;
        this.userRepository = userRepository;
    }

    public Mono<Map> analyzeAllergy(String kakaoId, LocalDate occurredDate, Map<String, Object> requestBody) {
        User user = userRepository.findById(kakaoId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<AllergyAnalysis> existingAnalysis = allergyAnalysisRepository.findByUserAndOccurredDate(user, occurredDate);
        if (existingAnalysis.isPresent()) {
            return Mono.just(convertToResponseMap(existingAnalysis.get()));
        }

        return webClient.post()
                .uri("")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> saveAnalysisResult(kakaoId, response, requestBody, occurredDate));
    }

    public Map getAnalysisResult(String kakaoId, LocalDate occurredDate) {
        User user = userRepository.findById(kakaoId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AllergyAnalysis analysis = allergyAnalysisRepository.findByUserAndOccurredDate(user, occurredDate)
                .orElseThrow(() -> new RuntimeException("Analysis not found"));

        return convertToResponseMap(analysis);
    }

    private Map saveAnalysisResult(String kakaoId, Map response, Map<String, Object> requestBody, LocalDate occurredDate) {
        User user = userRepository.findById(kakaoId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AllergyAnalysis analysis = AllergyAnalysis.builder()
                .user(user)
                .allergyInfo((String) requestBody.get("allergy_info"))
                .occurredDate(occurredDate)
                .analysisDate(LocalDateTime.now())
                .build();

        // 의심되는 약물 처리
        List<String> suspectedMeds = (List<String>) response.get("result");
        for (String medicationName : suspectedMeds) {
            SuspectedMedication medication = SuspectedMedication.builder()
                    .analysis(analysis)
                    .medicationName(medicationName)
                    .build();
            analysis.getSuspectedMedications().add(medication);
        }

        // 분석 이유 처리
        Map<String, List<String>> causes = (Map<String, List<String>>) response.get("cause");
        int reasonNumber = 1;
        for (Map.Entry<String, List<String>> entry : causes.entrySet()) {
            List<String> reasonInfo = entry.getValue();
            AnalysisReason reason = AnalysisReason.builder()
                    .analysis(analysis)
                    .reasonNumber(reasonNumber)
                    .reasonDescription(reasonInfo.get(0))
                    .relevance(reasonInfo.get(1))
                    .build();
            analysis.getAnalysisReasons().add(reason);
            reasonNumber++;
        }

        allergyAnalysisRepository.save(analysis);
        return response;
    }

    private Map convertToResponseMap(AllergyAnalysis analysis) {
        Map<String, Object> response = new HashMap<>();

        // 의심되는 약물 목록
        List<String> result = analysis.getSuspectedMedications().stream()
                .map(SuspectedMedication::getMedicationName)
                .collect(Collectors.toList());
        response.put("result", result);

        // 원인 분석
        Map<String, List<String>> causes = new HashMap<>();
        analysis.getAnalysisReasons().forEach(reason -> {
            String key = "reason" + reason.getReasonNumber();
            causes.put(key, Arrays.asList(reason.getReasonDescription(), reason.getRelevance()));
        });
        response.put("cause", causes);

        return response;
    }


    @Transactional
    public void deleteAnalysis(String kakaoId, LocalDate occurredDate) {
        User user = userRepository.findById(kakaoId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AllergyAnalysis analysis = allergyAnalysisRepository.findByUserAndOccurredDate(user, occurredDate)
                .orElseThrow(() -> new RuntimeException("Analysis not found"));

        allergyAnalysisRepository.delete(analysis);
    }
}