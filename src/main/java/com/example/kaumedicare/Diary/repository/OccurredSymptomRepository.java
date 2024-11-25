package com.example.kaumedicare.Diary.repository;

import com.example.kaumedicare.Diary.model.OccurredSymptom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OccurredSymptomRepository extends JpaRepository<OccurredSymptom, Long> {
    List<OccurredSymptom> findByDiaryUserKakaoIdAndDiaryDate(String kakaoId, LocalDate date);

    long countByDiaryId(Long diaryId);
}