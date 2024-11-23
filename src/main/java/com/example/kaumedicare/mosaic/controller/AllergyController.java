package com.example.kaumedicare.mosaic.controller;

import com.example.kaumedicare.mosaic.dto.AllergyRequestDto;
import com.example.kaumedicare.mosaic.service.AllergyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/allergy")
public class AllergyController {

    private final AllergyService allergyService;

    @Autowired // 의존성 주입
    public AllergyController(AllergyService allergyService) {
        this.allergyService = allergyService;
    }

    @PostMapping("/register") // POST 요청 처리 메서드 선언
    public ResponseEntity<?> registerAllergy(@RequestPart("image") MultipartFile image,
                                             @RequestPart("data") AllergyRequestDto allergyRequestDto) {
        try {
            String processedImageUrl = allergyService.processAndSaveImage(image); // 이미지 처리 및 저장
            return ResponseEntity.ok().body(processedImageUrl); // 성공 응답 반환
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing image: " + e.getMessage()); // 오류 응답 반환
        }
    }
}