package com.example.kaumedicare.Diary.repository;

import com.example.kaumedicare.Diary.model.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
    Optional<Diary> findByUserKakaoIdAndDate(String kakaoId, LocalDate date);
}