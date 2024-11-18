package com.example.kaumedicare.Medicine.dto;

import com.example.kaumedicare.Medicine.model.Medicine;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MedicineResponse {
    private Long id;
    private String entpName;    // 업체명
    private String itemName;    // 제품명
    private String itemSeq;     // 품목기준코드

    public static MedicineResponse from(Medicine medicine) {
        return MedicineResponse.builder()
                .id(medicine.getId())
                .entpName(medicine.getEntpName())
                .itemName(medicine.getItemName())
                .itemSeq(medicine.getItemSeq())
                .build();
    }
}
