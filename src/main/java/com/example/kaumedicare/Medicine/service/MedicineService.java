package com.example.kaumedicare.Medicine.service;

import com.example.kaumedicare.Medicine.model.Medicine;
import com.example.kaumedicare.Medicine.repository.MedicineRepository;
import com.example.kaumedicare.OpenAPI.service.MedicineApiService;
import com.example.kaumedicare.OpenAPI.service.MedicineDurApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final MedicineApiService medicineApiService;
    private final MedicineDurApiService medicineDurApiService;

    @Async
    public CompletableFuture<String> fetchAndSaveMainMedicines() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("기본 의약품 데이터 가져오기 시작");
                medicineApiService.fetchAndSaveAllMedicines(medicineRepository);
                log.info("기본 의약품 데이터 가져오기 완료");
                return "기본 의약품 데이터 가져오기 완료";
            } catch (Exception e) {
                log.error("기본 의약품 데이터 저장 중 오류 발생: ", e);
                throw new RuntimeException("기본 의약품 데이터 저장 중 오류가 발생했습니다.", e);
            }
        });
    }

    @Async
    public CompletableFuture<String> fetchAndSaveDurMedicines() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("DUR 의약품 데이터 가져오기 시작");
                medicineDurApiService.fetchAndSaveAllDurMedicines(medicineRepository);
                log.info("DUR 의약품 데이터 가져오기 완료");
                return "DUR 의약품 데이터 가져오기 완료";
            } catch (Exception e) {
                log.error("DUR 의약품 데이터 저장 중 오류 발생: ", e);
                throw new RuntimeException("DUR 의약품 데이터 저장 중 오류가 발생했습니다.", e);
            }
        });
    }

    @Async
    public CompletableFuture<String> fetchAndSaveAllMedicines() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("전체 의약품 데이터 가져오기 시작");
                CompletableFuture<String> mainMedicines = fetchAndSaveMainMedicines();
                CompletableFuture<String> durMedicines = fetchAndSaveDurMedicines();

                // 모든 작업이 완료될 때까지 대기
                CompletableFuture.allOf(mainMedicines, durMedicines).join();

                log.info("전체 의약품 데이터 가져오기 완료");
                return "전체 의약품 데이터 가져오기 완료";
            } catch (Exception e) {
                log.error("전체 의약품 데이터 저장 중 오류 발생: ", e);
                throw new RuntimeException("전체 의약품 데이터 저장 중 오류가 발생했습니다.", e);
            }
        });
    }

    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    public Optional<Medicine> getMedicineById(Long id) {
        return medicineRepository.findById(id);
    }
}