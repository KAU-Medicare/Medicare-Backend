package com.example.kaumedicare.HealthFood.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "healthfoods")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthFood {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String enterprise;
    @Column(length = 1000)
    private String product;
    @Column(nullable = false, unique = true)
    private String statementNo;
}