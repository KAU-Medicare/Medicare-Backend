package com.example.kaumedicare.Diary.model;

import com.example.kaumedicare.Medicine.model.Medicine;
import com.example.kaumedicare.User.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "medicine_inventory")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kakao_id", referencedColumnName = "kakao_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(nullable = false)
    private Integer currentQuantity;  // 현재 잔여량

    @Column(nullable = false)
    private Integer initialQuantity;  // 처음 등록한 수량

    @Column(nullable = false)
    private LocalDate registeredDate;  // 등록일
}