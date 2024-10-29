package com.example.kaumedicare.HealthFood.dto;

import com.example.kaumedicare.HealthFood.model.HealthFood;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HealthFoodResponse {
    private Long id;
    private String enterprise;
    private String product;
    private String statementNo;

    public static HealthFoodResponse from(HealthFood healthFood) {
        return HealthFoodResponse.builder()
                .id(healthFood.getId())
                .enterprise(healthFood.getEnterprise())
                .product(healthFood.getProduct())
                .statementNo(healthFood.getStatementNo())
                .build();
    }
}