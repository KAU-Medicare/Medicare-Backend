package com.example.kaumedicare.Diary.service;

import com.example.kaumedicare.Diary.dto.InventoryRequest;
import com.example.kaumedicare.Diary.dto.InventoryResponse;
import com.example.kaumedicare.Diary.dto.MedicineType;
import com.example.kaumedicare.Diary.dto.UpdateInventoryRequest;
import com.example.kaumedicare.Diary.model.Inventory;
import com.example.kaumedicare.Diary.repository.InventoryRepository;
import com.example.kaumedicare.Dur.repository.DurRepository;
import com.example.kaumedicare.Exception.DURConflictException;
import com.example.kaumedicare.Exception.EntityNotFoundException;
import com.example.kaumedicare.Exception.UnauthorizedException;
import com.example.kaumedicare.HealthFood.model.HealthFood;
import com.example.kaumedicare.HealthFood.repository.HealthFoodRepository;
import com.example.kaumedicare.Medicine.model.Medicine;
import com.example.kaumedicare.Medicine.repository.MedicineRepository;
import com.example.kaumedicare.User.model.User;
import com.example.kaumedicare.User.repository.UserRepository;
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
                .medicine(medicine)
                .healthFood(healthFood)
                .type(request.getType())
                .nickname(request.getNickname())
                .capsuleCount(request.getCapsuleCount())
                .useNotification(request.getUseNotification())
                .takingTime(request.getTakingTime())
                .takingDays(request.getTakingDays())
                .startDate(request.getStartDate())    // 시작일 설정
                .endDate(request.getEndDate())        // 종료일 설정
                .build();

        Inventory savedInventory = inventoryRepository.save(inventory);
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
    public void updateNickname(String kakaoId, Long id, String nickname) {
        Inventory inventory = findInventoryWithPermissionCheck(kakaoId, id);
        inventory.updateNickname(nickname);
    }

    @Transactional
    public void deleteInventory(String kakaoId, Long id) {
        Inventory inventory = findInventoryWithPermissionCheck(kakaoId, id);
        // 실제로 삭제하지 않고 종료일 설정
        inventory.setEndDate(LocalDate.now());
        inventoryRepository.save(inventory);
    }


    @Transactional
    public InventoryResponse updateInventory(String kakaoId, Long id, UpdateInventoryRequest request) {
        Inventory inventory = findInventoryWithPermissionCheck(kakaoId, id);

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
    public void checkTaken(String kakaoId, Long id, LocalDate date, boolean taken) {
        Inventory inventory = findInventoryWithPermissionCheck(kakaoId, id);

        if (taken) {
            // 복용 체크
            inventory.takeMedicine(date);
        } else {
            // 복용 체크 해제
            inventory.cancelTakeMedicine(date);
        }

        inventoryRepository.save(inventory);
    }

    public List<InventoryResponse> getDateInventories(String kakaoId, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return inventoryRepository.findByUserKakaoIdAndTakingDaysContaining(kakaoId, dayOfWeek)
                .stream()
                .filter(inventory -> isDateInRange(inventory, date))
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

    private boolean isDateInRange(Inventory inventory, LocalDate date) {
        // startDate부터 endDate까지의 범위 체크
        // endDate가 null이면 현재까지 계속 복용으로 간주
        return !date.isBefore(inventory.getStartDate()) &&
                (inventory.getEndDate() == null || !date.isAfter(inventory.getEndDate()));
    }

    private Inventory findInventoryWithPermissionCheck(String kakaoId, Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Inventory not found with id: %d", id)));

        if (!inventory.getUser().getKakaoId().equals(kakaoId)) {
            throw new UnauthorizedException(
                    String.format("User %s is not authorized to access inventory %d", kakaoId, id));
        }

        return inventory;
    }
}