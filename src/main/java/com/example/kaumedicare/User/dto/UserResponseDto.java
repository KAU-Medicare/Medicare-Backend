package com.example.kaumedicare.User.dto;

import com.example.kaumedicare.User.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private String kakaoId;
    private String nickname;
    private boolean isLoggedIn;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .kakaoId(user.getKakaoId())
                .nickname(user.getNickname())
                .isLoggedIn(user.isLoggedIn())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}