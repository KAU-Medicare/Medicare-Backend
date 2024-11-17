package com.example.kaumedicare.Diary.repository;

import com.example.kaumedicare.Diary.model.TakenMedicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TakenMedicineRepository extends JpaRepository<TakenMedicine, Long> {
    void deleteByMedicineId(Long medicineId);
}