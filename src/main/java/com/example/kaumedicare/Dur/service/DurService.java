package com.example.kaumedicare.Dur.service;

import com.example.kaumedicare.Dur.model.Dur;
import com.example.kaumedicare.Dur.repository.DurRepository;
import com.example.kaumedicare.OpenAPI.service.DurApiService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DurService {

    private final DurRepository durRepository;
    private final DurApiService durApiService;  // 추가

    @Transactional
    public void fetchAndSaveDurRelations() {
        durApiService.fetchAndSaveDurRelations();
    }

    public List<Dur> getAllDurRelations() {
        return durRepository.findAll();
    }

    public List<Dur> getDurRelationsByTargetMedicineId(Long targetId) {
        return durRepository.findByTargetMedicine_Id(targetId);
    }
}