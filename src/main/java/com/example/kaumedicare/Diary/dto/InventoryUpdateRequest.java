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
public class InventoryUpdateRequest {
    private String nickname;  // 별명 수정
    private List<DayOfWeek> takingDays;  // 복용 요일 수정
    private LocalTime takingTime;  // 복용 시간 수정
    private Integer capsuleCount;  // 1회 복용량 수정
    private Boolean useNotification;  // 알림 여부 수정
}