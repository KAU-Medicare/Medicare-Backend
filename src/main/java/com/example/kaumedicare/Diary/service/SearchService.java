package com.example.kaumedicare.Diary.service;

import com.example.kaumedicare.Diary.dto.HealthFoodSearchResponse;
import com.example.kaumedicare.Diary.dto.MedicineSearchResponse;
import com.example.kaumedicare.HealthFood.repository.HealthFoodRepository;
import com.example.kaumedicare.Medicine.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final MedicineRepository medicineRepository;
    private final HealthFoodRepository healthFoodRepository;

    public List<MedicineSearchResponse> searchMedicines(String keyword) {
        return medicineRepository.findByItemNameContaining(keyword)
                .stream()
                .map(medicine -> MedicineSearchResponse.builder()
                        .id(medicine.getId())
                        .itemName(medicine.getItemName())
                        .entpName(medicine.getEntpName())
                        .itemSeq(medicine.getItemSeq())
                        .build())
                .collect(Collectors.toList());
    }

    public List<HealthFoodSearchResponse> searchHealthFoods(String keyword) {
        return healthFoodRepository.findByProductContaining(keyword)
                .stream()
                .map(healthFood -> HealthFoodSearchResponse.builder()
                        .id(healthFood.getId())
                        .product(healthFood.getProduct())
                        .enterprise(healthFood.getEnterprise())
                        .statementNo(healthFood.getStatementNo())
                        .build())
                .collect(Collectors.toList());
    }
}