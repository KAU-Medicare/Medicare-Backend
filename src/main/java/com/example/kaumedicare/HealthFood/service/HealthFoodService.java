package com.example.kaumedicare.HealthFood.service;

import com.example.kaumedicare.HealthFood.dto.HealthFoodResponse;
import com.example.kaumedicare.HealthFood.repository.HealthFoodRepository;
import com.example.kaumedicare.OpenAPI.service.HealthFoodApiService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthFoodService {

    private final HealthFoodRepository healthFoodRepository;
    private final HealthFoodApiService healthFoodApiService;

    @Transactional
    public void fetchAndSaveHealthFoods() {
        try {
            healthFoodApiService.fetchAndSaveAllHealthFoods(healthFoodRepository);
            log.info("Health food data fetch and save completed");
        } catch (Exception e) {
            log.error("Error during health food fetch and save: ", e);
            throw new RuntimeException("건강기능식품 데이터 저장 중 오류가 발생했습니다.", e);
        }
    }

    public List<HealthFoodResponse> getAllHealthFoods() {
        return healthFoodRepository.findAll().stream()
                .map(HealthFoodResponse::from)
                .toList();
    }

    public Optional<HealthFoodResponse> getHealthFoodById(Long id) {
        return healthFoodRepository.findById(id)
                .map(HealthFoodResponse::from);
    }
}
