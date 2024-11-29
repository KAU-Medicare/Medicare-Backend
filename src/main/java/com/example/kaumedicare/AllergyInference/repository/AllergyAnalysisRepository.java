package com.example.kaumedicare.AllergyInference.repository;

import com.example.kaumedicare.AllergyInference.model.AllergyAnalysis;
import com.example.kaumedicare.User.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AllergyAnalysisRepository extends JpaRepository<AllergyAnalysis, Long> {
    Optional<AllergyAnalysis> findByUserAndOccurredDate(User user, LocalDate occurredDate);
}