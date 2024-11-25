package com.example.kaumedicare.Diary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecordSymptomRequest {
    private String kakaoId;
    private List<Long> symptomIds;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate occurredDate;  // 발생 날짜

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;     // 시작 시간

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;       // 종료 시간

    private String base64Image;

    public void validate() {
        // 필수 필드 검증
        if (kakaoId == null || symptomIds == null || occurredDate == null
                || startTime == null || endTime == null) {
            throw new IllegalArgumentException("필수 필드가 누락되었습니다.");
        }

        // 시간 순서 검증
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("종료 시간이 시작 시간보다 빠를 수 없습니다.");
        }

        // 미래 날짜 검증
        if (occurredDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("미래 날짜는 입력할 수 없습니다.");
        }
    }
}