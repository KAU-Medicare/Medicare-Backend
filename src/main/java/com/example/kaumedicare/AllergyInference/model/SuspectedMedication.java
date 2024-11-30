package com.example.kaumedicare.AllergyInference.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "suspected_medications")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuspectedMedication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id")
    private AllergyAnalysis analysis;

    @Column(nullable = false)
    private String medicationName;
}