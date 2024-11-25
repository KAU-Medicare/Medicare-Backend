package com.example.kaumedicare.Diary.dto;

import com.example.kaumedicare.Diary.model.Inventory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
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
    private LocalTime takingTime;
    private List<DayOfWeek> takingDays;
    private boolean taken;
    private LocalDate startDate;  // 추가
    private LocalDate endDate;    // 추가

    public static InventoryResponse from(Inventory inventory) {
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
                .taken(false)
                .startDate(inventory.getStartDate())  // 추가
                .endDate(inventory.getEndDate())      // 추가
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