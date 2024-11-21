package com.example.kaumedicare.Diary.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HealthFoodSearchResponse {
    private Long id;
    private String product;
    private String enterprise;
    private String statementNo;
}