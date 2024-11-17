package com.example.kaumedicare.Diary.repository;

import com.example.kaumedicare.Diary.model.TakenHealthFood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TakenHealthFoodRepository extends JpaRepository<TakenHealthFood, Long> {
    void deleteByHealthFoodId(Long healthFoodId);
}