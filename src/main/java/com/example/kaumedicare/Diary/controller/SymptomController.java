package com.example.kaumedicare.Diary.controller;

import com.example.kaumedicare.Diary.dto.OccurredSymptomResponse;
import com.example.kaumedicare.Diary.dto.RecordSymptomRequest;
import com.example.kaumedicare.Diary.dto.SymptomResponse;
import com.example.kaumedicare.Diary.service.SymptomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "알레르기 정보 등록 요청",
            required = true,
            content = @Content(examples = {
                    @ExampleObject(
                            name = "등록 예시",
                            value = """
                                    {
                                        "kakaoId": "3763697930",
                                        "symptomIds": [1, 2],
                                        "occurredDate": "2024-11-25",
                                        "startTime": "19:03:43",
                                        "endTime": "20:03:43",
                                        "base64Image": "base64_encoded_string"
                                    }
                                    """
                    )
            })
    )
    @PostMapping("/records")
    public ResponseEntity<OccurredSymptomResponse> recordSymptom(
            @RequestBody RecordSymptomRequest request
    ) {
        return ResponseEntity.ok(symptomService.recordSymptom(request));
    }

    @Operation(summary = "특정 날짜의 알레르기 정보 조회")
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "조회 성공 예시",
                                    value = """
                                            [{
                                                "id": 1,
                                                "symptomNames": ["두통", "어지러움"],
                                                "occurredDate": "2024-11-25",
                                                "startTime": "19:03:43",
                                                "endTime": "20:03:43",
                                                "base64Image": "base64_encoded_string"
                                            }]
                                            """
                            )
                    }
            )
    )
    @GetMapping("/records/{kakaoId}")
    public ResponseEntity<List<OccurredSymptomResponse>> getSymptomsByDate(
            @PathVariable String kakaoId,
            @RequestParam LocalDate date
    ) {
        return ResponseEntity.ok(symptomService.getSymptomsByDate(kakaoId, date));
    }

    @Operation(summary = "기간별 알레르기 정보 조회")
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "조회 성공 예시",
                                    value = """
                                        [{
                                            "id": 1,
                                            "symptomNames": ["두통", "어지러움"],
                                            "occurredDate": "2024-11-25",
                                            "startTime": "19:03:43",
                                            "endTime": "20:03:43",
                                            "base64Image": "base64_encoded_string"
                                        }]
                                        """
                            )
                    }
            )
    )
    @GetMapping("/records/{kakaoId}/period")
    public ResponseEntity<List<OccurredSymptomResponse>> getSymptomsByPeriod(
            @PathVariable String kakaoId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        return ResponseEntity.ok(symptomService.getSymptomsByPeriod(kakaoId, startDate, endDate));
    }


    @Operation(summary = "알레르기 정보 수정")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "알레르기 정보 수정 요청",
            required = true,
            content = @Content(examples = {
                    @ExampleObject(
                            name = "수정 예시",
                            value = """
                                    {
                                        "kakaoId": "3763697930",
                                        "symptomIds": [1, 2],
                                        "occurredDate": "2024-11-25",
                                        "startTime": "19:03:43",
                                        "endTime": "20:03:43",
                                        "base64Image": "base64_encoded_string"
                                    }
                                    """
                    )
            })
    )
    @PutMapping("/records/{id}")
    public ResponseEntity<OccurredSymptomResponse> updateSymptom(
            @PathVariable Long id,
            @RequestBody RecordSymptomRequest request
    ) {
        return ResponseEntity.ok(symptomService.updateSymptom(id, request));
    }



    @Operation(summary = "알레르기 정보 삭제")
    @DeleteMapping("/records/{kakaoId}/{id}")
    public ResponseEntity<Void> deleteSymptom(
            @Parameter(description = "사용자 카카오 ID") @PathVariable String kakaoId,
            @Parameter(description = "알레르기 기록 ID") @PathVariable Long id
    ) {
        symptomService.deleteSymptom(kakaoId, id);
        return ResponseEntity.ok().build();
    }

}