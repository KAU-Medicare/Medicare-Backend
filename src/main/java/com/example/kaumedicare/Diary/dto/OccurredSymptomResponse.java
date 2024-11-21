package com.example.kaumedicare.Diary.dto;

import com.example.kaumedicare.Diary.model.OccurredSymptom;
import lombok.Builder;
import lombok.Getter;
import com.example.kaumedicare.Diary.model.Symptom;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class OccurredSymptomResponse {
    private Long id;
    private List<String> symptomNames;  // 여러 증상 이름
    private LocalDateTime occurredDateTime;
    private String base64Image;

    public static OccurredSymptomResponse from(OccurredSymptom occurredSymptom) {
        return OccurredSymptomResponse.builder()
                .id(occurredSymptom.getId())
                .symptomNames(occurredSymptom.getSymptoms().stream()
                        .map(Symptom::getName)
                        .collect(Collectors.toList()))
                .occurredDateTime(occurredSymptom.getOccurredDateTime())
                .base64Image(occurredSymptom.getBase64Image())
                .build();
    }
}