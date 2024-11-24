package com.example.kaumedicare.Diary.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TakenRecord {
    @Column(name = "taken_time")
    private LocalDateTime takenTime;

    @Column(name = "taken")
    private boolean taken;

    public void update(LocalDateTime time, boolean taken) {
        this.takenTime = time;
        this.taken = taken;
    }
}