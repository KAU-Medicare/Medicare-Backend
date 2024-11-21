package com.example.kaumedicare.Diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecordSymptomRequest {
    private String kakaoId;
    private List<Long> symptomIds;
    private LocalDateTime occurredDateTime;
    private String base64Image;  // base64로 인코딩된 이미지
}