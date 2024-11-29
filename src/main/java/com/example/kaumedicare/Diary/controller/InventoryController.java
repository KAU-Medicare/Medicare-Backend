package com.example.kaumedicare.Diary.controller;

import com.example.kaumedicare.Diary.dto.InventoryRequest;
import com.example.kaumedicare.Diary.dto.InventoryResponse;
import com.example.kaumedicare.Diary.dto.UpdateInventoryRequest;
import com.example.kaumedicare.Diary.dto.UpdateItemNicknameRequest;
import com.example.kaumedicare.Diary.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory API", description = "복약 관리 API")
public class InventoryController {
    private final InventoryService inventoryService;

    @Operation(summary = "약/영양제 등록")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "약/영양제 등록 정보",
            required = true,
            content = @Content(examples = {
                    @ExampleObject(
                            name = "약 등록 예시",
                            value = """
                                    {
                                        "kakaoId": "3763697930",
                                        "itemId": 1,
                                        "type": "MEDICINE",
                                        "nickname": "활명수별명",
                                        "capsuleCount": 2,
                                        "useNotification": true,
                                        "takingTime": "12:00:00",
                                        "takingDays": ["MONDAY"],
                                        "startDate": "2024-11-23",
                                        "endDate": null
                                    }
                                    """
                    )
            })
    )
    @PostMapping
    public ResponseEntity<InventoryResponse> registerInventory(
            @RequestBody InventoryRequest request
    ) {
        return ResponseEntity.ok(inventoryService.register(request));
    }

    @Operation(summary = "사용자의 모든 약/영양제 조회", description = "특정 사용자의 모든 약/영양제 목록을 조회합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    value = """
                                            [{
                                                "id": 1,
                                                "itemName": "활명수",
                                                "nickname": "활명수별명",
                                                "type": "MEDICINE",
                                                "capsuleCount": 2,
                                                "useNotification": true,
                                                "takingTime": "12:00:00",
                                                "takingDays": ["MONDAY"],
                                                "taken": false,
                                                "startDate": "2024-11-23",
                                                "endDate": null
                                            }]
                                            """
                            )
                    }
            )
    )
    @GetMapping("/user/{kakaoId}")
    public ResponseEntity<List<InventoryResponse>> getUserInventories(
            @Parameter(description = "사용자 카카오 ID") @PathVariable String kakaoId
    ) {
        return ResponseEntity.ok(inventoryService.getUserInventories(kakaoId));
    }

    @Operation(summary = "오늘 복용할 약/영양제 조회", description = "오늘 복용해야 하는 약/영양제 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/today/{kakaoId}")
    public ResponseEntity<List<InventoryResponse>> getTodayInventories(
            @Parameter(description = "사용자 카카오 ID") @PathVariable String kakaoId
    ) {
        return ResponseEntity.ok(inventoryService.getTodayInventories(kakaoId));
    }

    @Operation(summary = "약/영양제 별명 수정", description = "등록된 약/영양제의 별명을 수정합니다.")
    @PutMapping("/{kakaoId}/{id}/nickname")
    public ResponseEntity<Void> updateNickname(
            @Parameter(description = "사용자 카카오 ID") @PathVariable String kakaoId,
            @Parameter(description = "약/영양제 ID") @PathVariable Long id,
            @RequestBody UpdateItemNicknameRequest request
    ) {
        inventoryService.updateNickname(kakaoId, id, request.getNickname());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "복용 여부 체크", description = "특정 날짜의 약/영양제 복용 여부를 체크합니다.")
    @PutMapping("/{kakaoId}/{id}/taken")
    public ResponseEntity<Void> checkTaken(

            @Parameter(description = "사용자 카카오 ID") @PathVariable String kakaoId,
            @Parameter(description = "약/영양제 ID") @PathVariable Long id,
            @Parameter(description = "복용 날짜 (yyyy-MM-dd)") @RequestParam LocalDate date,
            @Parameter(description = "복용 여부 (true/false)") @RequestParam boolean taken
    ) {
        log.info("Check taken request - kakaoId: {}, id: {}", kakaoId, id);
        inventoryService.checkTaken(kakaoId, id, date, taken);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "약/영양제 삭제", description = "등록된 약/영양제를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 약/영양제를 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음")
    })
    @DeleteMapping("/{kakaoId}/{id}")
    public ResponseEntity<Void> deleteInventory(
            @Parameter(description = "사용자 카카오 ID") @PathVariable String kakaoId,
            @Parameter(description = "약/영양제 ID") @PathVariable Long id
    ) {
        log.info("Delete request - kakaoId: {}, id: {}", kakaoId, id);
        inventoryService.deleteInventory(kakaoId, id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "약/영양제 정보 수정")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "약/영양제 수정 정보",
            required = true,
            content = @Content(examples = {
                    @ExampleObject(
                            name = "수정 예시",
                            value = """
                                {
                                    "nickname": "활명수별명",
                                    "capsuleCount": 2,
                                    "useNotification": true,
                                    "takingTime": "12:00:00",
                                    "takingDays": ["MONDAY"],
                                    "startDate": "2024-11-23",
                                    "endDate": null
                                }
                                """
                    )
            })
    )
    @PutMapping("/{kakaoId}/{id}")
    public ResponseEntity<InventoryResponse> updateInventory(
            @Parameter(description = "사용자 카카오 ID") @PathVariable String kakaoId,
            @Parameter(description = "약/영양제 ID") @PathVariable Long id,
            @RequestBody UpdateInventoryRequest request
    ) {
        return ResponseEntity.ok(inventoryService.updateInventory(kakaoId, id, request));
    }


    @Operation(summary = "특정 날짜의 복용 목록 조회", description = "특정 날짜에 복용해야 하는 약/영양제 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/date/{kakaoId}")
    public ResponseEntity<List<InventoryResponse>> getDateInventories(
            @Parameter(description = "사용자 카카오 ID") @PathVariable String kakaoId,
            @Parameter(description = "조회 날짜 (yyyy-MM-dd)") @RequestParam LocalDate date
    ) {
        return ResponseEntity.ok(inventoryService.getDateInventories(kakaoId, date));
    }
}