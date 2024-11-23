package com.example.kaumedicare.Diary.repository;

import com.example.kaumedicare.Diary.dto.MedicineType;
import com.example.kaumedicare.Diary.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByUserKakaoId(String kakaoId);

    List<Inventory> findByUserKakaoIdAndType(String kakaoId, MedicineType type);

    List<Inventory> findByUserKakaoIdAndTakingDaysContaining(String kakaoId, DayOfWeek dayOfWeek);


}