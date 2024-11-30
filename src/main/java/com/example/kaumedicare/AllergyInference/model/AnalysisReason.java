package com.example.kaumedicare.AllergyInference.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "analysis_reasons")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisReason {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id")
    private AllergyAnalysis analysis;

    @Column(nullable = false)
    private Integer reasonNumber;

    @Column(nullable = false, length = 500)
    private String reasonDescription;

    @Column(nullable = false)
    private String relevance;
}