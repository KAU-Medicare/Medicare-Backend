package com.example.kaumedicare.AllergyInference.repository;

import com.example.kaumedicare.AllergyInference.model.AllergyAnalysis;
import com.example.kaumedicare.User.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllergyAnalysisRepository extends JpaRepository<AllergyAnalysis, Long> {
    List<AllergyAnalysis> findByUserOrderByAnalysisDateDesc(User user);
    // 필요하다면 다른 쿼리 메서드도 추가할 수 있습니다
}