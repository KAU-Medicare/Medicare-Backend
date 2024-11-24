package com.example.kaumedicare.StandardCode.repository;

import com.example.kaumedicare.StandardCode.model.StandardCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StandardCodeRepository extends JpaRepository<StandardCode, Long> {
    Optional<StandardCode> findByStandardCode(String standardCode);
}