package com.example.kaumedicare.Medicine.controller;


import com.example.kaumedicare.Exception.EntityNotFoundException;
import com.example.kaumedicare.Medicine.dto.MedicineResponse;
import com.example.kaumedicare.Medicine.model.Medicine;
import com.example.kaumedicare.Medicine.service.MedicineService;
import com.example.kaumedicare.StandardCode.service.StandardCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;
    private final StandardCodeService standardCodeService;

    @PostMapping("/fetch/main")
    public ResponseEntity<String> fetchMainMedicines() {
        try {
            medicineService.fetchAndSaveMainMedicines();
            return ResponseEntity.ok("기본 의약품 데이터 가져오기가 백그라운드에서 시작되었습니다.");
        } catch (Exception e) {
            log.error("기본 의약품 데이터 가져오기 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("기본 의약품 데이터 가져오기 실패: " + e.getMessage());
        }
    }

    @PostMapping("/fetch/dur")
    public ResponseEntity<String> fetchDurMedicines() {
        try {
            medicineService.fetchAndSaveDurMedicines();
            return ResponseEntity.ok("DUR 의약품 데이터 가져오기가 백그라운드에서 시작되었습니다.");
        } catch (Exception e) {
            log.error("DUR 의약품 데이터 가져오기 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("DUR 의약품 데이터 가져오기 실패: " + e.getMessage());
        }
    }

    @PostMapping("/fetch/all")
    public ResponseEntity<String> fetchAllMedicines() {
        try {
            medicineService.fetchAndSaveAllMedicines();
            return ResponseEntity.ok("전체 의약품 데이터 가져오기가 백그라운드에서 시작되었습니다.");
        } catch (Exception e) {
            log.error("전체 의약품 데이터 가져오기 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("전체 의약품 데이터 가져오기 실패: " + e.getMessage());
        }
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

    // API to update DB to latest data
    @PostMapping("/update-standard-codes")
    public ResponseEntity<String> updateStandardCodes() {
        standardCodeService.updateStandardCodes();
        return ResponseEntity.ok("의약품 표준코드 데이터 업데이트가 완료되었습니다.");
    }

    @GetMapping("/standard-code/{standardCode}")
    public ResponseEntity<MedicineResponse> findByStandardCode(@PathVariable String standardCode) {
        String itemSeq = standardCodeService.findItemSeqByStandardCode(standardCode);
        Medicine medicine = medicineService.findByItemSeq(itemSeq)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("품목기준코드 %s에 해당하는 의약품을 찾을 수 없습니다.", itemSeq)));

        return ResponseEntity.ok(MedicineResponse.from(medicine));
    }
}