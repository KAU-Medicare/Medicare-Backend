package com.example.kaumedicare.HealthFood.service;

import com.example.kaumedicare.HealthFood.dto.HealthFoodResponse;
import com.example.kaumedicare.HealthFood.repository.HealthFoodRepository;
import com.example.kaumedicare.OpenAPI.service.HealthFoodApiService;
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
public class HealthFoodService {

    private final HealthFoodRepository healthFoodRepository;
    private final HealthFoodApiService healthFoodApiService;

    @Async
    public CompletableFuture<String> fetchAndSaveHealthFoods() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("건강기능식품 데이터 가져오기 시작");
                healthFoodApiService.fetchAndSaveAllHealthFoods(healthFoodRepository);
                log.info("건강기능식품 데이터 가져오기 완료");
                return "건강기능식품 데이터 가져오기 완료";
            } catch (Exception e) {
                log.error("건강기능식품 데이터 저장 중 오류 발생: ", e);
                throw new RuntimeException("건강기능식품 데이터 저장 중 오류가 발생했습니다.", e);
            }
        });
    }

    public List<HealthFoodResponse> getAllHealthFoods() {
        try {
            return healthFoodRepository.findAll().stream()
                    .map(HealthFoodResponse::from)
                    .toList();
        } catch (Exception e) {
            log.error("건강기능식품 전체 목록 조회 중 오류 발생: ", e);
            throw new RuntimeException("건강기능식품 목록 조회 중 오류가 발생했습니다.", e);
        }
    }

    public Optional<HealthFoodResponse> getHealthFoodById(Long id) {
        try {
            return healthFoodRepository.findById(id)
                    .map(HealthFoodResponse::from);
        } catch (Exception e) {
            log.error("건강기능식품 단일 조회 중 오류 발생. id: {}", id, e);
            throw new RuntimeException("건강기능식품 조회 중 오류가 발생했습니다.", e);
        }
    }
}