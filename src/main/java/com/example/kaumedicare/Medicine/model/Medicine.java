package com.example.kaumedicare.Medicine.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "medicines")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Medicine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String entpName;    // 업체명

    @Column(length = 1000)
    private String itemName;    // 제품명

    @Column(nullable = false, unique = true)
    private String itemSeq;     // 품목기준코드
}