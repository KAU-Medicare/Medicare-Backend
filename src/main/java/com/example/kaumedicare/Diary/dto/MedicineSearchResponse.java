package com.example.kaumedicare.Diary.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MedicineSearchResponse {
    private Long id;
    private String itemName;
    private String entpName;
    private String itemSeq;
}