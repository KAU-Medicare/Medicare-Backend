package com.example.kaumedicare.Diary.controller;

import com.example.kaumedicare.Diary.dto.OccurredSymptomResponse;
import com.example.kaumedicare.Diary.dto.RecordSymptomRequest;
import com.example.kaumedicare.Diary.dto.SymptomResponse;
import com.example.kaumedicare.Diary.service.SymptomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/symptoms")
@RequiredArgsConstructor
@Tag(name = "Symptoms API", description = "증상/알레르기 관리 API")
public class SymptomController {
    private final SymptomService symptomService;

    @Operation(summary = "모든 증상 목록 조회")
    @GetMapping
    public ResponseEntity<List<SymptomResponse>> getAllSymptoms() {
        return ResponseEntity.ok(symptomService.getAllSymptoms());
    }

    @Operation(summary = "알레르기 정보 등록")
    @PostMapping("/records")
    public ResponseEntity<OccurredSymptomResponse> recordSymptom(
            @RequestBody RecordSymptomRequest request
    ) {
        return ResponseEntity.ok(symptomService.recordSymptom(request));
    }

    @Operation(summary = "특정 날짜의 알레르기 정보 조회")
    @GetMapping("/records/{kakaoId}")
    public ResponseEntity<List<OccurredSymptomResponse>> getSymptomsByDate(
            @PathVariable String kakaoId,
            @RequestParam LocalDate date
    ) {
        return ResponseEntity.ok(symptomService.getSymptomsByDate(kakaoId, date));
    }

    @Operation(summary = "알레르기 정보 수정")
    @PutMapping("/records/{id}")
    public ResponseEntity<OccurredSymptomResponse> updateSymptom(
            @PathVariable Long id,
            @RequestBody RecordSymptomRequest request
    ) {
        return ResponseEntity.ok(symptomService.updateSymptom(id, request));
    }

    @Operation(summary = "알레르기 정보 삭제")
    @DeleteMapping("/records/{id}")
    public ResponseEntity<Void> deleteSymptom(@PathVariable Long id) {
        symptomService.deleteSymptom(id);
        return ResponseEntity.ok().build();
    }
}