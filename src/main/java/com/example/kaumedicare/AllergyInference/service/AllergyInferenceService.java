package com.example.kaumedicare.AllergyInference.service;

import com.example.kaumedicare.AllergyInference.model.AllergyAnalysis;
import com.example.kaumedicare.AllergyInference.model.AnalysisReason;
import com.example.kaumedicare.AllergyInference.model.SuspectedMedication;
import com.example.kaumedicare.AllergyInference.repository.AllergyAnalysisRepository;
import com.example.kaumedicare.User.model.User;
import com.example.kaumedicare.User.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
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

    public Mono<Map> analyzeAllergy(String kakaoId, Map<String, Object> requestBody) {
        return webClient.post()
                .uri("")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> saveAnalysisResult(kakaoId, response, requestBody));
    }

    private Map saveAnalysisResult(String kakaoId, Map response, Map<String, Object> requestBody) {
        User user = userRepository.findById(kakaoId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // AllergyAnalysis 생성
        AllergyAnalysis analysis = AllergyAnalysis.builder()
                .user(user)
                .allergyInfo((String) requestBody.get("allergy_info"))
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
        return response;  // 원본 응답 반환
    }
}