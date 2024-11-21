package com.example.kaumedicare.Diary.model;

import com.example.kaumedicare.Diary.dto.MedicineType;
import com.example.kaumedicare.HealthFood.model.HealthFood;
import com.example.kaumedicare.Medicine.model.Medicine;
import com.example.kaumedicare.User.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;


@Entity
@Table(name = "inventories")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kakao_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_food_id")
    private HealthFood healthFood;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MedicineType type;

    private String nickname;

    @Column(nullable = false)
    private Integer capsuleCount;

    private Boolean useNotification;

    @Column(nullable = false)
    private LocalTime takingTime;

    @ElementCollection
    @CollectionTable(
            name = "taking_days",
            joinColumns = @JoinColumn(name = "inventory_id")
    )
    @Column(name = "day_of_week")
    private List<DayOfWeek> takingDays;


    @ElementCollection
    @CollectionTable(
            name = "taken_records",
            joinColumns = @JoinColumn(name = "inventory_id")
    )
    private Map<LocalDate, LocalDateTime> takenRecords = new HashMap<>();  // 날짜별 복용 시간 기록

    public void takeMedicine(LocalDate date) {
        takenRecords.put(date, LocalDateTime.now());  // 현재 시간으로 복용 기록
    }

    public void cancelTakeMedicine(LocalDate date) {
        takenRecords.remove(date);
    }

    public boolean isTakenOnDate(LocalDate date) {
        return takenRecords.containsKey(date);
    }

    public LocalDateTime getTakenTimeOnDate(LocalDate date) {
        return takenRecords.get(date);
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateTakingDays(List<DayOfWeek> takingDays) {
        this.takingDays = takingDays;
    }

    public void updateTakingTime(LocalTime takingTime) {
        this.takingTime = takingTime;
    }

    public void updateCapsuleCount(Integer capsuleCount) {
        this.capsuleCount = capsuleCount;
    }

    public void updateUseNotification(Boolean useNotification) {
        this.useNotification = useNotification;
    }
}



