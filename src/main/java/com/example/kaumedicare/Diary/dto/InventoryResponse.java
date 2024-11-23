package com.example.kaumedicare.Diary.dto;

import com.example.kaumedicare.Diary.model.Inventory;
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
public class InventoryResponse {
    private Long id;
    private String itemName;  // medicine의 itemName 또는 healthFood의 product
    private String nickname;
    private MedicineType type;
    private Integer capsuleCount;
    private Boolean useNotification;
    private LocalTime takingTime;
    private List<DayOfWeek> takingDays;
    private boolean taken;

    public static InventoryResponse from(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .itemName(inventory.getType() == MedicineType.MEDICINE ?
                        inventory.getMedicine().getItemName() :
                        inventory.getHealthFood().getProduct())
                .nickname(inventory.getNickname())
                .type(inventory.getType())
                .capsuleCount(inventory.getCapsuleCount())
                .useNotification(inventory.getUseNotification())
                .takingTime(inventory.getTakingTime())
                .takingDays(inventory.getTakingDays())
                .taken(inventory.isTakenOnDate(LocalDate.now()))  // 오늘 날짜 기준 복용 여부
                .build();
    }
}