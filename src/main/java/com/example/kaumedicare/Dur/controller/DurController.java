package com.example.kaumedicare.Dur.controller;

import com.example.kaumedicare.Dur.dto.DurResponse;
import com.example.kaumedicare.Dur.model.Dur;
import com.example.kaumedicare.Dur.service.DurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dur")
@RequiredArgsConstructor
public class DurController {

    private final DurService durService;

    @PostMapping("/fetch")
    public ResponseEntity<String> fetchDurRelations() {
        durService.fetchAndSaveDurRelations();
        return ResponseEntity.ok("병용금기 관계 데이터 가져오기 완료");
    }

    @GetMapping
    public ResponseEntity<List<DurResponse>> getAllDurRelations() {
        List<Dur> durRelations = durService.getAllDurRelations();
        List<DurResponse> response = durRelations.stream()
                .map(DurResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{targetId}")
    public ResponseEntity<List<DurResponse>> getDurRelationsByTargetId(@PathVariable Long targetId) {
        List<Dur> durRelations = durService.getDurRelationsByTargetMedicineId(targetId);
        List<DurResponse> response = durRelations.stream()
                .map(DurResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }
}
