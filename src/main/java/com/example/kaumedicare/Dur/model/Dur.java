package com.example.kaumedicare.Dur.model;

import com.example.kaumedicare.Medicine.model.Medicine;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dur")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Dur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "target_medicine_id", nullable = false)
    private Medicine targetMedicine;

    @ManyToOne
    @JoinColumn(name = "dur_medicine_id", nullable = false)
    private Medicine durMedicine;

    public Dur(Medicine targetMedicine, Medicine durMedicine) {
        this.targetMedicine = targetMedicine;
        this.durMedicine = durMedicine;
    }
}

