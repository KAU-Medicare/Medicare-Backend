package com.example.kaumedicare.AllergyInference.model;

import com.example.kaumedicare.User.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "allergy_analysis")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllergyAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kakao_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String allergyInfo;

    @Column(nullable = false)
    private LocalDate occurredDate;  // 알레르기 발생일

    @Column(nullable = false)
    private LocalDateTime analysisDate;  // 분석 수행일

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SuspectedMedication> suspectedMedications = new ArrayList<>();

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnalysisReason> analysisReasons = new ArrayList<>();
}