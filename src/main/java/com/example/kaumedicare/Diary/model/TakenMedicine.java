package com.example.kaumedicare.Diary.model;

import com.example.kaumedicare.Medicine.model.Medicine;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "taken_medicines")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TakenMedicine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = false)
    private Diary diary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(nullable = false)
    private LocalDateTime takenDateTime;

    private boolean isTaken;  // 복용 여부
}