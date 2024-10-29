package com.example.kaumedicare.Medicine.controller;


import com.example.kaumedicare.Medicine.dto.MedicineResponse;
import com.example.kaumedicare.Medicine.model.Medicine;
import com.example.kaumedicare.Medicine.service.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    @PostMapping("/fetch")
    public ResponseEntity<String> fetchMedicines() {
        medicineService.fetchAndSaveMedicines();
        return ResponseEntity.ok("의약품 데이터 가져오기 완료");
    }

    @GetMapping
    public ResponseEntity<List<MedicineResponse>> getAllMedicines() {
        List<Medicine> medicines = medicineService.getAllMedicines();
        List<MedicineResponse> response = medicines.stream()
                .map(MedicineResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicineResponse> getMedicineById(@PathVariable Long id) {
        return medicineService.getMedicineById(id)
                .map(MedicineResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

