package com.example.kaumedicare.Dur.repository;

import com.example.kaumedicare.Dur.model.Dur;
import com.example.kaumedicare.Medicine.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DurRepository extends JpaRepository<Dur, Long> {
    boolean existsByTargetMedicineAndDurMedicine(Medicine targetMedicine, Medicine durMedicine);

    List<Dur> findByTargetMedicine_Id(Long targetId);

}
