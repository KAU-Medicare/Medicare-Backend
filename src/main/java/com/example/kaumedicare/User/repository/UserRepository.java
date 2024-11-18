package com.example.kaumedicare.User.repository;

import com.example.kaumedicare.User.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByKakaoId(String kakaoId);

    Optional<User> findByNickname(String nickname);
}