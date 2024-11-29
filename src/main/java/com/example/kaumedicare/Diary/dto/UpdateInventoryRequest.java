package com.example.kaumedicare.Diary.dto;

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
public class UpdateInventoryRequest {
    private String nickname;
    private Integer capsuleCount;
    private Boolean useNotification;
    private LocalTime takingTime;
    private List<DayOfWeek> takingDays;
    private LocalDate startDate;
    private LocalDate endDate;
}