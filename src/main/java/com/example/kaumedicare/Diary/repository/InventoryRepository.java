package com.example.kaumedicare.Diary.repository;

import com.example.kaumedicare.Diary.dto.MedicineType;
import com.example.kaumedicare.Diary.model.Inventory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByUserKakaoIdAndType(String kakaoId, MedicineType type);

    List<Inventory> findByUserKakaoIdAndTakingDaysContaining(String kakaoId, DayOfWeek dayOfWeek);

    @Query("SELECT i FROM Inventory i WHERE i.user.kakaoId = :kakaoId " +
            "AND i.startDate <= :referenceDate " +
            "AND (i.endDate IS NULL OR i.endDate >= :referenceDate)")
    List<Inventory> findCurrentInventoriesByKakaoId(
            @Param("kakaoId") String kakaoId,
            @Param("referenceDate") LocalDate referenceDate
    );

    @Query("SELECT i FROM Inventory i " +
            "WHERE i.user.kakaoId = :kakaoId " +
            "AND i.type = :type " +
            "AND (i.endDate IS NULL OR i.endDate >= :startDate)")
    List<Inventory> findInventoriesActiveAfterStartDate(
            @Param("kakaoId") String kakaoId,
            @Param("type") MedicineType type,
            @Param("startDate") LocalDate startDate
    );

    List<Inventory> findByUseNotificationTrue();

    @Query(nativeQuery = true, value =
            "SELECT * FROM inventories i " +
                    "WHERE i.use_notification = 1 " +
                    "AND i.taking_time = ?1 " +  // 직접 문자열 비교로 변경
                    "AND EXISTS (SELECT 1 FROM taking_days td " +
                    "           WHERE td.inventory_id = i.id " +
                    "           AND td.day_of_week = ?2)")
    @EntityGraph(attributePaths = {"user", "medicine", "healthFood"})
    List<Inventory> findByUseNotificationTrueAndTakingTimeAndTakingDaysContaining(
            String time,  // LocalTime 대신 String으로 변경
            DayOfWeek dayOfWeek);
}