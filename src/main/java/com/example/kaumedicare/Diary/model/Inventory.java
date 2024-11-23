package com.example.kaumedicare.Diary.model;

import com.example.kaumedicare.Diary.dto.LocalTimeAttributeConverter;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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


    @Column(name = "start_date")
    private LocalDate startDate;  // 복용 시작일

    @Column(name = "end_date")
    private LocalDate endDate;    // 복용 종료일 (null이면 계속 복용)
    // takenRecords는 실제 복용 기록을 저장
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "taken_records",
            joinColumns = @JoinColumn(name = "inventory_id")
    )
    @MapKeyColumn(name = "taken_records_key")
    private Map<LocalDate, TakenRecord> takenRecords = new HashMap<>();

    @Builder
    public Inventory(Long id, User user, Medicine medicine, HealthFood healthFood,
                     MedicineType type, String nickname, Integer capsuleCount,
                     Boolean useNotification, LocalTime takingTime, List<DayOfWeek> takingDays,
                     LocalDate startDate, LocalDate endDate) {  // startDate, endDate 추가
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
        this.startDate = startDate;  // 시작일 설정
        this.endDate = endDate;      // 종료일 설정
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void takeMedicine(LocalDate date) {
        if (takenRecords == null) {
            takenRecords = new HashMap<>();
        }

        if (takenRecords.containsKey(date)) {
            TakenRecord record = takenRecords.get(date);
            record.update(LocalDateTime.now(), true);
        } else {
            takenRecords.put(date, new TakenRecord(LocalDateTime.now(), true));
        }
    }

    public void cancelTakeMedicine(LocalDate date) {
        if (takenRecords != null && takenRecords.containsKey(date)) {
            TakenRecord record = takenRecords.get(date);
            record.update(LocalDateTime.now(), false);
        }
    }

    public boolean isTakenOnDate(LocalDate date) {
        if (takenRecords == null || !takenRecords.containsKey(date)) {
            return false;
        }
        return takenRecords.get(date).isTaken();
    }

    public void initializeTakenRecords() {
        if (takenRecords == null) {
            takenRecords = new HashMap<LocalDate, TakenRecord>();
        }
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



