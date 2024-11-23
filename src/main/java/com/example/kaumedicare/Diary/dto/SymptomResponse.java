package com.example.kaumedicare.Diary.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SymptomResponse {
    private Long id;
    private String name;
}