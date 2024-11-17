
/*
package com.example.kaumedicare.Diary.controller;

import com.example.kaumedicare.Diary.dto.*;
import com.example.kaumedicare.Diary.service.DiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/medicine-diary")
@RequiredArgsConstructor
@Tag(name = "Diary", description = "복약일지 관련 API")
public class DiaryController {

    private final DiaryService medicineDiaryService;
    private final UserDetailsServiceImpl userDetailsService;

    @PostMapping
    @Operation(summary = "복약일지 생성", description = "특정 날짜의 복약일지를 생성합니다.")
    public ResponseEntity<IdResponse> createDiary(@RequestBody DiaryRequest request) {
        IdResponse response = medicineDiaryService.createDiary(
                userDetailsService.loadCurrentUser(),
                request
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{diaryId}/element")
    @Operation(summary = "복약일지 요소 추가", description = "복약일지에 약 또는 영양제 복용 기록을 추가합니다.")
    public ResponseEntity<IdResponse> addDiaryElement(
            @PathVariable Long diaryId,
            @RequestBody DiaryElementCreateRequest request) {
        IdResponse response = medicineDiaryService.addDiaryElement(
                userDetailsService.loadCurrentUser(),
                diaryId,
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{diaryId}/element")
    @Operation(summary = "복약일지 요소 삭제", description = "복약일지에서 약 또는 영양제 복용 기록을 삭제합니다.")
    public ResponseEntity<IdResponse> deleteDiaryElement(
            @PathVariable Long diaryId,
            @Valid @RequestBody DiaryElementDeleteRequest request) {
        IdResponse response = medicineDiaryService.deleteDiaryElement(
                userDetailsService.loadCurrentUser(),
                diaryId,
                request
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "복약일지 조회", description = "특정 날짜의 복약일지를 조회합니다.")
    public ResponseEntity<DiaryResponse> getDiary(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        DiaryResponse response = medicineDiaryService.getDiary(
                userDetailsService.loadCurrentUser(),
                date
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/period")
    @Operation(summary = "기간별 복약일지 조회", description = "특정 기간의 복약일지 목록을 조회합니다.")
    public ResponseEntity<List<DiaryResponse>> getDiaryPeriod(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<DiaryResponse> response = medicineDiaryService.getDiaryPeriod(
                userDetailsService.loadCurrentUser(),
                startDate,
                endDate
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{diaryId}")
    @Operation(summary = "복약일지 삭제", description = "특정 복약일지를 삭제합니다.")
    public ResponseEntity<IdResponse> deleteDiary(@PathVariable Long diaryId) {
        IdResponse response = medicineDiaryService.deleteDiary(
                userDetailsService.loadCurrentUser(),
                diaryId
        );
        return ResponseEntity.ok(response);
    }
}

 */