package com.example.kaumedicare.Diary.model;

import com.example.kaumedicare.HealthFood.model.HealthFood;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "taken_health_foods")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TakenHealthFood {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = false)
    private Diary diary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_food_id", nullable = false)
    private HealthFood healthFood;

    @Column(nullable = false)
    private LocalDateTime takenDateTime;

    private boolean isTaken;
}