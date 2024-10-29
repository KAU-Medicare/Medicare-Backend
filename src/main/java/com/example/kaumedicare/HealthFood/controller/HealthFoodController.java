package com.example.kaumedicare.HealthFood.controller;


import com.example.kaumedicare.HealthFood.dto.HealthFoodResponse;
import com.example.kaumedicare.HealthFood.service.HealthFoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/healthfoods")
@RequiredArgsConstructor
public class HealthFoodController {

    private final HealthFoodService healthFoodService;

    @PostMapping("/fetch")
    public ResponseEntity<String> fetchHealthFoods() {
        healthFoodService.fetchAndSaveHealthFoods();
        return ResponseEntity.ok("건강기능식품 데이터 가져오기 완료");
    }

    @GetMapping
    public ResponseEntity<List<HealthFoodResponse>> getAllHealthFoods() {
        List<HealthFoodResponse> response = healthFoodService.getAllHealthFoods();
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<HealthFoodResponse> getHealthFoodById(@PathVariable Long id) {
        return healthFoodService.getHealthFoodById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
