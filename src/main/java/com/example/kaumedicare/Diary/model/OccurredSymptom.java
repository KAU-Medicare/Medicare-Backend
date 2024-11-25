package com.example.kaumedicare.Diary.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToMany
    @JoinTable(
            name = "occurred_symptom_items",
            joinColumns = @JoinColumn(name = "occurred_symptom_id"),
            inverseJoinColumns = @JoinColumn(name = "symptom_id", nullable = false)
    )
    private List<Symptom> symptoms = new ArrayList<>();

    @Column(nullable = false)
    private LocalDate occurredDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(name = "base64Image")
    private String base64Image;

    // 증상 추가 메서드
    public void addSymptom(Symptom symptom) {
        this.symptoms.add(symptom);
    }

    public void updateSymptoms(List<Symptom> symptoms) {
        this.symptoms = symptoms;
    }

    public void updateOccurredDate(LocalDate occurredDate) {
        this.occurredDate = occurredDate;
    }

    public void updateStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void updateEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public void updateImageUrl(String base64Image) {
        this.base64Image = base64Image;
    }
}