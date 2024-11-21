package com.example.kaumedicare.Diary.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
            inverseJoinColumns = @JoinColumn(name = "symptom_id")
    )
    private List<Symptom> symptoms = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime occurredDateTime;

    @Column(name = "base64Image")
    private String base64Image;

    // 증상 추가 메서드
    public void addSymptom(Symptom symptom) {
        this.symptoms.add(symptom);
    }

    public void updateSymptoms(List<Symptom> symptoms) {
        this.symptoms = symptoms;
    }

    public void updateOccurredDateTime(LocalDateTime occurredDateTime) {
        this.occurredDateTime = occurredDateTime;
    }

    public void updateImageUrl(String base64Image) {
        this.base64Image = base64Image;
    }
}