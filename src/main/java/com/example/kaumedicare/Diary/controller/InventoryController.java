package com.example.kaumedicare.Diary.controller;

import com.example.kaumedicare.Diary.dto.*;
import com.example.kaumedicare.Diary.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory API", description = "복약 관리 API")
public class InventoryController {
    private final InventoryService inventoryService;

    @Operation(summary = "약/영양제 등록", description = "새로운 약 또는 영양제를 복용 목록에 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "409", description = "병용금기 약물 존재")
    })
    @PostMapping
    public ResponseEntity<InventoryResponse> registerInventory(
            @RequestBody InventoryRequest request
    ) {
        return ResponseEntity.ok(inventoryService.register(request));
    }

    @Operation(summary = "사용자의 모든 약/영양제 조회", description = "특정 사용자의 모든 약/영양제 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 약/영양제를 찾을 수 없음")
    })
    @PutMapping("/{id}/nickname")
    public ResponseEntity<Void> updateNickname(
            @Parameter(description = "약/영양제 ID") @PathVariable Long id,
            @RequestBody UpdateItemNicknameRequest request
    ) {
        inventoryService.updateNickname(id, request.getNickname());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "약/영양제 삭제", description = "등록된 약/영양제를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 약/영양제를 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(
            @Parameter(description = "약/영양제 ID") @PathVariable Long id
    ) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "약/영양제 정보 수정", description = "등록된 약/영양제의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 약/영양제를 찾을 수 없음")
    })
    @PutMapping("/{id}")
    public ResponseEntity<InventoryResponse> updateInventory(
            @Parameter(description = "약/영양제 ID") @PathVariable Long id,
            @RequestBody UpdateInventoryRequest request
    ) {
        return ResponseEntity.ok(inventoryService.updateInventory(id, request));
    }

    @Operation(summary = "복용 여부 체크", description = "특정 날짜의 약/영양제 복용 여부를 체크합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "체크 성공"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 약/영양제를 찾을 수 없음")
    })
    @PutMapping("/{id}/taken")
    public ResponseEntity<Void> checkTaken(
            @Parameter(description = "약/영양제 ID") @PathVariable Long id,
            @Parameter(description = "복용 날짜 (yyyy-MM-dd)") @RequestParam LocalDate date,
            @Parameter(description = "복용 여부 (true/false)") @RequestParam boolean taken
    ) {
        inventoryService.checkTaken(id, date, taken);
        return ResponseEntity.ok().build();
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