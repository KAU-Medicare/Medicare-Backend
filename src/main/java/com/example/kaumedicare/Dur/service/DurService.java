package com.example.kaumedicare.Dur.service;

import com.example.kaumedicare.Dur.model.Dur;
import com.example.kaumedicare.Dur.repository.DurRepository;
import com.example.kaumedicare.OpenAPI.service.DurApiService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class DurService {

    private final DurRepository durRepository;
    private final DurApiService durApiService;

    @Async
    public CompletableFuture<String> fetchAndSaveDurRelations() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("병용금기 관계 데이터 가져오기 시작");
                durApiService.fetchAndSaveDurRelations();
                log.info("병용금기 관계 데이터 가져오기 완료");
                return "병용금기 관계 데이터 가져오기 완료";
            } catch (Exception e) {
                log.error("병용금기 관계 데이터 저장 중 오류 발생: ", e);
                throw new RuntimeException("병용금기 관계 데이터 저장 중 오류가 발생했습니다.", e);
            }
        });
    }

    public List<Dur> getAllDurRelations() {
        return durRepository.findAll();
    }

    public List<Dur> getDurRelationsByTargetMedicineId(Long targetId) {
        return durRepository.findByTargetMedicine_Id(targetId);
    }
}