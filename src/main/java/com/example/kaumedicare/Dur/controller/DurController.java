package com.example.kaumedicare.Dur.controller;

import com.example.kaumedicare.Dur.dto.DurResponse;
import com.example.kaumedicare.Dur.model.Dur;
import com.example.kaumedicare.Dur.service.DurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/dur")
@RequiredArgsConstructor
public class DurController {

    private final DurService durService;

    @PostMapping("/fetch")
    public ResponseEntity<String> fetchDurRelations() {
        try {
            durService.fetchAndSaveDurRelations();
            return ResponseEntity.ok("병용금기 관계 데이터 가져오기가 백그라운드에서 시작되었습니다.");
        } catch (Exception e) {
            log.error("병용금기 관계 데이터 가져오기 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("병용금기 관계 데이터 가져오기 실패: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<DurResponse>> getAllDurRelations() {
        try {
            List<Dur> durRelations = durService.getAllDurRelations();
            List<DurResponse> response = durRelations.stream()
                    .map(DurResponse::from)
                    .toList();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("병용금기 관계 데이터 조회 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{targetId}")
    public ResponseEntity<List<DurResponse>> getDurRelationsByTargetId(@PathVariable Long targetId) {
        try {
            List<Dur> durRelations = durService.getDurRelationsByTargetMedicineId(targetId);
            List<DurResponse> response = durRelations.stream()
                    .map(DurResponse::from)
                    .toList();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("특정 약품의 병용금기 관계 데이터 조회 실패. targetId: {}", targetId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}