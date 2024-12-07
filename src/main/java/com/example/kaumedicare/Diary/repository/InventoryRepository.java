package com.example.kaumedicare.Diary.repository;

import com.example.kaumedicare.Diary.dto.MedicineType;
import com.example.kaumedicare.Diary.model.Inventory;
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

    @Query("SELECT i FROM Inventory i WHERE i.useNotification = true " +
            "AND TO_CHAR(i.takingTime, 'HH24:MI') = TO_CHAR(:time, 'HH24:MI') " +
            "AND :dayOfWeek MEMBER OF i.takingDays")
    List<Inventory> findByUseNotificationTrueAndTakingTimeAndTakingDaysContaining(
            LocalTime time, DayOfWeek dayOfWeek);
}