package com.example.kaumedicare.Medicine.service;

import com.example.kaumedicare.Medicine.model.Medicine;
import com.example.kaumedicare.Medicine.repository.MedicineRepository;
import com.example.kaumedicare.OpenAPI.service.MedicineApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final MedicineApiService medicineApiService;

    @Transactional
    public void fetchAndSaveMedicines() {
        try {
            medicineApiService.fetchAndSaveAllMedicines(medicineRepository);
            log.info("Medicine data fetch and save completed");
        } catch (Exception e) {
            log.error("Error during medicine fetch and save: ", e);
            throw new RuntimeException("의약품 데이터 저장 중 오류가 발생했습니다.", e);
        }
    }

    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    public Optional<Medicine> getMedicineById(Long id) {
        return medicineRepository.findById(id);
    }
}