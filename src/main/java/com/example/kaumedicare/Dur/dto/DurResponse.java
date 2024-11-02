package com.example.kaumedicare.Dur.dto;

import com.example.kaumedicare.Dur.model.Dur;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DurResponse {
    private Long targetMedicineId;
    private Long durMedicineId;

    public static DurResponse from(Dur dur) {
        return DurResponse.builder()
                .targetMedicineId(dur.getTargetMedicine().getId())
                .durMedicineId(dur.getDurMedicine().getId())
                .build();
    }
}

