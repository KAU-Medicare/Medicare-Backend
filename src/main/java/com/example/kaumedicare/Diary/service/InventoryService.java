package com.example.kaumedicare.Diary.service;

import com.example.kaumedicare.Diary.dto.*;
import com.example.kaumedicare.Diary.model.Inventory;
import com.example.kaumedicare.Diary.repository.InventoryRepository;
import com.example.kaumedicare.Dur.repository.DurRepository;
import com.example.kaumedicare.HealthFood.model.HealthFood;
import com.example.kaumedicare.HealthFood.repository.HealthFoodRepository;
import com.example.kaumedicare.Medicine.model.Medicine;
import com.example.kaumedicare.Medicine.repository.MedicineRepository;
import com.example.kaumedicare.User.model.User;
import com.example.kaumedicare.User.repository.UserRepository;
import com.example.kaumedicare.Exception.DURConflictException;
import com.example.kaumedicare.Exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final MedicineRepository medicineRepository;
    private final HealthFoodRepository healthFoodRepository;
    private final UserRepository userRepository;
    private final DurRepository durRepository;

    @Transactional
    public InventoryResponse register(InventoryRequest request) {
        User user = userRepository.findByKakaoId(request.getKakaoId())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("User not found with kakaoId: %s", request.getKakaoId())));

        if (request.getType() == MedicineType.MEDICINE) {
            checkDURConflict(request.getItemId(), request.getKakaoId());
        }

        Medicine medicine = null;
        HealthFood healthFood = null;

        if (request.getType() == MedicineType.MEDICINE) {
            medicine = medicineRepository.findById(request.getItemId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("Medicine not found with id: %d", request.getItemId())));
        } else {
            healthFood = healthFoodRepository.findById(request.getItemId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("HealthFood not found with id: %d", request.getItemId())));
        }

        Inventory inventory = Inventory.builder()
                .user(user)
                .medicine(request.getType() == MedicineType.MEDICINE ?
                        medicineRepository.findById(request.getItemId())
                                .orElseThrow(() -> new RuntimeException("Medicine not found")) : null)
                .healthFood(request.getType() == MedicineType.HEALTH_FOOD ?
                        healthFoodRepository.findById(request.getItemId())
                                .orElseThrow(() -> new RuntimeException("HealthFood not found")) : null)
                .type(request.getType())
                .nickname(request.getNickname())
                .capsuleCount(request.getCapsuleCount())
                .useNotification(request.getUseNotification())
                .takingTime(request.getTakingTime())
                .takingDays(request.getTakingDays())
                .build();

        inventory.initializeTakenRecords();  // 명시적 초기화
        Inventory savedInventory = inventoryRepository.save(inventory);

        // 새로 등록된 경우에는 taken 상태를 false로 설정
        return InventoryResponse.from(savedInventory);
    }

    private void checkDURConflict(Long newMedicineId, String kakaoId) {
        Medicine newMedicine = medicineRepository.findById(newMedicineId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Medicine not found with id: %d", newMedicineId)));

        List<Inventory> userMedicines = inventoryRepository
                .findByUserKakaoIdAndType(kakaoId, MedicineType.MEDICINE);

        for (Inventory inv : userMedicines) {
            if (inv.getMedicine() != null &&
                    durRepository.existsByTargetMedicineAndDurMedicine(newMedicine, inv.getMedicine())) {
                throw new DURConflictException(
                        String.format("병용금기 약물이 존재합니다: %s", inv.getMedicine().getItemName()));
            }
        }
    }

    public List<InventoryResponse> getUserInventories(String kakaoId) {
        return inventoryRepository.findByUserKakaoId(kakaoId).stream()
                .map(InventoryResponse::from)
                .collect(Collectors.toList());
    }

    public List<InventoryResponse> getTodayInventories(String kakaoId) {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        return inventoryRepository.findByUserKakaoIdAndTakingDaysContaining(kakaoId, today)
                .stream()
                .map(InventoryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateNickname(Long id, String nickname) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));
        inventory.updateNickname(nickname);
    }

    @Transactional
    public void deleteInventory(Long id) {
        inventoryRepository.deleteById(id);
    }

    @Transactional
    public InventoryResponse updateInventory(Long id, UpdateInventoryRequest request) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        // 수정할 필드들만 업데이트
        if (request.getNickname() != null) {
            inventory.updateNickname(request.getNickname());
        }
        if (request.getTakingDays() != null) {
            inventory.updateTakingDays(request.getTakingDays());
        }
        if (request.getTakingTime() != null) {
            inventory.updateTakingTime(request.getTakingTime());
        }
        if (request.getCapsuleCount() != null) {
            inventory.updateCapsuleCount(request.getCapsuleCount());
        }
        if (request.getUseNotification() != null) {
            inventory.updateUseNotification(request.getUseNotification());
        }

        return InventoryResponse.from(inventory);
    }

    @Transactional
    public void checkTaken(Long inventoryId, LocalDate date, boolean taken) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        if (taken) {
            inventory.takeMedicine(date);
        } else {
            inventory.cancelTakeMedicine(date);
        }
    }

    public List<InventoryResponse> getDateInventories(String kakaoId, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return inventoryRepository.findByUserKakaoIdAndTakingDaysContaining(kakaoId, dayOfWeek)
                .stream()
                .map(inventory -> InventoryResponse.builder()
                        .id(inventory.getId())
                        .itemName(inventory.getType() == MedicineType.MEDICINE ?
                                inventory.getMedicine().getItemName() :
                                inventory.getHealthFood().getProduct())
                        .nickname(inventory.getNickname())
                        .type(inventory.getType())
                        .takingTime(inventory.getTakingTime())
                        .capsuleCount(inventory.getCapsuleCount())
                        .taken(inventory.isTakenOnDate(date))
                        .build())
                .collect(Collectors.toList());
    }
}