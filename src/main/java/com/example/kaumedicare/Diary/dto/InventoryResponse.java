package com.example.kaumedicare.Diary.dto;

import com.example.kaumedicare.Diary.model.Inventory;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {
    private Long id;
    private String itemName;
    private String nickname;
    private MedicineType type;
    private Integer capsuleCount;
    private Boolean useNotification;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime takingTime;

    private List<DayOfWeek> takingDays;
    private boolean taken;

    public static InventoryResponse from(Inventory inventory) {
        // null 체크를 포함한 안전한 변환
        return InventoryResponse.builder()
                .id(inventory.getId())
                .itemName(getItemNameSafely(inventory))
                .nickname(inventory.getNickname())
                .type(inventory.getType())
                .capsuleCount(inventory.getCapsuleCount())
                .useNotification(inventory.getUseNotification())
                .takingTime(inventory.getTakingTime())
                .takingDays(inventory.getTakingDays() != null ?
                        new ArrayList<>(inventory.getTakingDays()) :
                        new ArrayList<>())
                .taken(false)  // 새로 등록시에는 기본적으로 false
                .build();
    }

    private static String getItemNameSafely(Inventory inventory) {
        if (inventory.getType() == MedicineType.MEDICINE) {
            return inventory.getMedicine() != null ?
                    inventory.getMedicine().getItemName() : "Unknown Medicine";
        } else {
            return inventory.getHealthFood() != null ?
                    inventory.getHealthFood().getProduct() : "Unknown Health Food";
        }
    }
}