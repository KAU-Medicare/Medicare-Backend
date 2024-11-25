package com.example.kaumedicare.Dur.repository;

import com.example.kaumedicare.Dur.model.Dur;
import com.example.kaumedicare.Medicine.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DurRepository extends JpaRepository<Dur, Long> {
    boolean existsByTargetMedicineAndDurMedicine(Medicine targetMedicine, Medicine durMedicine);

    List<Dur> findByTargetMedicine_Id(Long targetId);

    @Query("SELECT COUNT(d) > 0 FROM Dur d WHERE " +
            "(d.targetMedicine.id = :medicineId1 AND d.durMedicine.id = :medicineId2) " +
            "OR (d.targetMedicine.id = :medicineId2 AND d.durMedicine.id = :medicineId1)")
    boolean existsDurConflictByIds(@Param("medicineId1") Long medicineId1, @Param("medicineId2") Long medicineId2);

}
