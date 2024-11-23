package com.example.kaumedicare.Diary.controller;

import com.example.kaumedicare.Diary.dto.HealthFoodSearchResponse;
import com.example.kaumedicare.Diary.dto.MedicineSearchResponse;
import com.example.kaumedicare.Diary.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "Search API", description = "약/영양제 검색 API")
public class SearchController {
    private final SearchService searchService;

    @Operation(summary = "약 검색", description = "약 이름으로 검색합니다.")
    @GetMapping("/medicines")
    public ResponseEntity<List<MedicineSearchResponse>> searchMedicines(
            @Parameter(description = "검색어 (예: 타이레놀)") @RequestParam String keyword
    ) {
        return ResponseEntity.ok(searchService.searchMedicines(keyword));
    }

    @Operation(summary = "영양제 검색", description = "영양제 이름으로 검색합니다.")
    @GetMapping("/health-foods")
    public ResponseEntity<List<HealthFoodSearchResponse>> searchHealthFoods(
            @Parameter(description = "검색어 (예: 비타민)") @RequestParam String keyword
    ) {
        return ResponseEntity.ok(searchService.searchHealthFoods(keyword));
    }
}