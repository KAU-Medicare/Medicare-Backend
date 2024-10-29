package com.example.kaumedicare.Medicine.repository;

import com.example.kaumedicare.Medicine.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    boolean existsByItemSeq(String itemSeq);
}
