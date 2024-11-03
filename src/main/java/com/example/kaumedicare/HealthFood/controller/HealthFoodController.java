package com.example.kaumedicare.HealthFood.controller;


import com.example.kaumedicare.HealthFood.dto.HealthFoodResponse;
import com.example.kaumedicare.HealthFood.service.HealthFoodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/healthfoods")
@RequiredArgsConstructor
public class HealthFoodController {

    private final HealthFoodService healthFoodService;

    @PostMapping("/fetch")
    public ResponseEntity<String> fetchHealthFoods() {
        try {
            healthFoodService.fetchAndSaveHealthFoods();
            return ResponseEntity.ok("건강기능식품 데이터 가져오기가 백그라운드에서 시작되었습니다.");
        } catch (Exception e) {
            log.error("건강기능식품 데이터 가져오기 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("건강기능식품 데이터 가져오기 실패: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<HealthFoodResponse>> getAllHealthFoods() {
        try {
            List<HealthFoodResponse> response = healthFoodService.getAllHealthFoods();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("건강기능식품 목록 조회 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<HealthFoodResponse> getHealthFoodById(@PathVariable Long id) {
        try {
            return healthFoodService.getHealthFoodById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("건강기능식품 단일 조회 실패. id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
