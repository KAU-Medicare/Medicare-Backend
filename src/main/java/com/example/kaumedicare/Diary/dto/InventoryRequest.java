package com.example.kaumedicare.Diary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryRequest {
    private String kakaoId;
    private Long itemId;
    private MedicineType type;
    private String nickname;
    private Integer capsuleCount;
    private Boolean useNotification;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime takingTime;

    private List<DayOfWeek> takingDays;

    private LocalDate startDate;  // 복용 시작일
    private LocalDate endDate;    // 복용 종료일 (선택)
}