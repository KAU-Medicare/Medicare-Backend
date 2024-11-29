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
    private LocalDate occurredDate;

    @Column(nullable = false)
    private LocalDateTime analysisDate;

    @Builder.Default  // 이 부분 추가
    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SuspectedMedication> suspectedMedications = new ArrayList<>();

    @Builder.Default  // 이 부분 추가
    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnalysisReason> analysisReasons = new ArrayList<>();
}