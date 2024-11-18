package com.example.kaumedicare.mosaic.dto;

import java.util.List;

public class AllergyRequestDto {
    private String occurrenceTime; // 알레르기 발생 시간
    private List<String> symptoms; // 증상 목록

    // Getters and setters
    public String getOccurrenceTime() {
        return occurrenceTime; // 발생 시간 반환
    }

    public void setOccurrenceTime(String occurrenceTime) {
        this.occurrenceTime = occurrenceTime; // 발생 시간 설정
    }

    public List<String> getSymptoms() {
        return symptoms; // 증상 목록 반환
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms; // 증상 목록 설정
    }
}