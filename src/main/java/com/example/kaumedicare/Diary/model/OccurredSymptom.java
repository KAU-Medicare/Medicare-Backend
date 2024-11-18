package com.example.kaumedicare.Diary.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "occurred_symptoms")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OccurredSymptom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = false)
    private Diary diary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "symptom_id", nullable = false)
    private Symptom symptom;

    @Column(nullable = false)
    private LocalDateTime occurredDateTime;

    private String imageUrl;  // 필요한 경우에만 사용
}