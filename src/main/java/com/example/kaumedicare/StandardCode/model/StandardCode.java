package com.example.kaumedicare.StandardCode.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "standard_codes")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandardCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String standardCode;  // 표준코드

    @Column(nullable = false)
    private String itemSeq;      // 품목기준코드
}
