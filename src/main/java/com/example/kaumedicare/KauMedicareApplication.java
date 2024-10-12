package com.example.kaumedicare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class KauMedicareApplication {

    public static void main(String[] args) {
        SpringApplication.run(KauMedicareApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(
                                "https://localhost:8080",// 로컬 Vue.js 개발 서버
                                "https://172.17.152.188:8080",
                                "https://172.30.1.24:8080",     // 팀원의 로컬 환경
                                "https://kau-medicare.shop"    // 프로덕션 환경
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}