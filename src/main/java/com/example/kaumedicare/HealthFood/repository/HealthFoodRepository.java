package com.example.kaumedicare.HealthFood.repository;

import com.example.kaumedicare.HealthFood.model.HealthFood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HealthFoodRepository extends JpaRepository<HealthFood, Long> {
    boolean existsByStatementNo(String statementNo);
}