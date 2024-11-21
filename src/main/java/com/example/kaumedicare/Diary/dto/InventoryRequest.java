package com.example.kaumedicare.Diary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryRequest {
    private String kakaoId;
    private Long itemId;  // medicine_id 또는 health_food_id
    private MedicineType type;
    private String nickname;
    private Integer capsuleCount;
    private Boolean useNotification;
    private LocalTime takingTime;
    private List<DayOfWeek> takingDays;
}