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
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InventoryService {
    private static final ZoneId KOREA_TIMEZONE = ZoneId.of("Asia/Seoul");
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

        // 미래 날짜 체크
        if (request.getStartDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("시작일은 미래 날짜가 될 수 없습니다.");
        }

        // DUR 확인 (약물인 경우에만)
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
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
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
                    durRepository.existsDurConflictByIds(newMedicine.getId(), inv.getMedicine().getId())) {
                throw new DURConflictException(
                        String.format("병용금기 약물이 존재합니다: %s", inv.getMedicine().getItemName()));
            }
        }
    }

    // 현재 활성화된 인벤토리 조회
    public List<InventoryResponse> getUserInventories(String kakaoId) {
        LocalDate today = LocalDate.now(KOREA_TIMEZONE);
        return inventoryRepository.findCurrentInventoriesByKakaoId(kakaoId, today)
                .stream()
                .filter(inventory -> isDateInRange(inventory, today))
                .map(InventoryResponse::from)
                .collect(Collectors.toList());
    }

    public List<InventoryResponse> getTodayInventories(String kakaoId) {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        LocalDate now = LocalDate.now(KOREA_TIMEZONE);

        return inventoryRepository.findByUserKakaoIdAndTakingDaysContaining(kakaoId, today)
                .stream()
                .filter(inventory -> isDateInRange(inventory, now)) // 날짜 범위 체크 추가
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
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // soft delete 처리 시 endDate를 오늘의 전날로 설정
        if (inventory.getStartDate().isEqual(today)) {
            inventoryRepository.deleteById(id); // hard delete
        } else {
            inventory.setEndDate(today.minusDays(1)); // endDate를 어제로 설정
            inventoryRepository.save(inventory);
        }
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
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        if (!isDateInRange(inventory, date) || date.isAfter(today)) {
            throw new IllegalArgumentException("복용 기록을 수정할 수 없는 날짜입니다.");
        }


        try {
            if (taken) {
                inventory.takeMedicine(date);
            } else {
                inventory.cancelTakeMedicine(date);
            }
            inventoryRepository.save(inventory);
        } catch (OptimisticLockingFailureException e) {
            throw new ConcurrentModificationException("다른 사용자가 동시에 수정하고 있습니다. 다시 시도해주세요.");
        }
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

    public boolean isDateInRange(Inventory inventory, LocalDate date) {
        if (date.isBefore(inventory.getStartDate())) {
            return false; // 시작일 이전은 제외
        }
        return inventory.getEndDate() == null || !date.isAfter(inventory.getEndDate()); // 종료일 이후는 제외
// 시작일과 종료일 사이 포함
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