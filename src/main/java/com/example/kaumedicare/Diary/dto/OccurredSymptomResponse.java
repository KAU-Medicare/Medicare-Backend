package com.example.kaumedicare.Diary.dto;

import com.example.kaumedicare.Diary.model.OccurredSymptom;
import com.example.kaumedicare.Diary.model.Symptom;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class OccurredSymptomResponse {
    private Long id;
    private List<String> symptomNames;
    private LocalDate occurredDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String base64Image;

    public static OccurredSymptomResponse from(OccurredSymptom occurredSymptom) {
        return OccurredSymptomResponse.builder()
                .id(occurredSymptom.getId())
                .symptomNames(occurredSymptom.getSymptoms().stream()
                        .map(Symptom::getName)
                        .collect(Collectors.toList()))
                .occurredDate(occurredSymptom.getOccurredDate())
                .startTime(occurredSymptom.getStartTime())
                .endTime(occurredSymptom.getEndTime())
                .base64Image(occurredSymptom.getBase64Image())
                .build();
    }
}