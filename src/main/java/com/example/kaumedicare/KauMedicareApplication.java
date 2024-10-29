package com.example.kaumedicare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class KauMedicareApplication {
    public static void main(String[] args) {
        SpringApplication.run(KauMedicareApplication.class, args);
    }
}