package com.example.kaumedicare.User.dto;

import com.example.kaumedicare.User.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSaveRequestDto {
    private String kakaoId;
    private String nickname;

    public User toEntity() {
        return User.builder()
                .kakaoId(kakaoId)
                .nickname(nickname)
                .isLoggedIn(false)
                .build();
    }
}