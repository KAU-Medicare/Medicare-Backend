package com.example.kaumedicare.Diary.model;

import com.example.kaumedicare.Diary.dto.LocalTimeAttributeConverter;
import com.example.kaumedicare.Diary.dto.MedicineType;
import com.example.kaumedicare.HealthFood.model.HealthFood;
import com.example.kaumedicare.Medicine.model.Medicine;
import com.example.kaumedicare.User.model.User;
import com.fasterxml.jackson.annotation.JsonFormat;
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

    @Column(name = "taking_time")
    @Convert(converter = LocalTimeAttributeConverter.class)
    private LocalTime takingTime;

    @ElementCollection
    @CollectionTable(
            name = "taking_days",
            joinColumns = @JoinColumn(name = "inventory_id")
    )
    @Column(name = "day_of_week")
    private List<DayOfWeek> takingDays;


    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "taken_records",
            joinColumns = @JoinColumn(name = "inventory_id")
    )
    @MapKeyColumn(name = "taken_date")
    @Column(name = "taken_time")
    private Map<LocalDate, LocalDateTime> takenRecords;

    public boolean isTakenOnDate(LocalDate date) {
        if (takenRecords == null) {
            return false;  // 안전한 기본값 반환
        }
        return takenRecords.containsKey(date);
    }

    public void initializeTakenRecords() {
        if (takenRecords == null) {
            takenRecords = new HashMap<>();
        }
    }

    @PrePersist
    @PreUpdate
    public void prePersist() {
        initializeTakenRecords();
    }

    @Builder
    public Inventory(Long id, User user, Medicine medicine, HealthFood healthFood,
                     MedicineType type, String nickname, Integer capsuleCount,
                     Boolean useNotification, LocalTime takingTime, List<DayOfWeek> takingDays) {
        this.id = id;
        this.user = user;
        this.medicine = medicine;
        this.healthFood = healthFood;
        this.type = type;
        this.nickname = nickname;
        this.capsuleCount = capsuleCount;
        this.useNotification = useNotification;
        this.takingTime = takingTime;
        this.takingDays = takingDays != null ? new ArrayList<>(takingDays) : new ArrayList<>();
        this.takenRecords = new HashMap<>();
    }
    public void takeMedicine(LocalDate date) {
        takenRecords.put(date, LocalDateTime.now());  // 현재 시간으로 복용 기록
    }

    public void cancelTakeMedicine(LocalDate date) {
        takenRecords.remove(date);
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



