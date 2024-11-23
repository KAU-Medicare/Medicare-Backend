package com.example.kaumedicare.Diary.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class RegisterMedicineRequest {
    private Long medicineId;
    private String kakaoId;
    private LocalTime takeTime;
    private List<DayOfWeek> takeDays;
    private Integer quantity;
}