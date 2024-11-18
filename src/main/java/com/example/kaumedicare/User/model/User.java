package com.example.kaumedicare.User.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity //JPA 엔티티임을 나타냄
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")  // user -> users로 변경
@Builder
public class User {
    // 카카오 고유 ID, @Id: 기본키(Primary Key) 지정
    @Id
    @Column(name = "kakao_id")  // 명시적으로 컬럼명 지정
    private String kakaoId;

    // 사용자 닉네임, @Column(nullable = false): NULL 값을 허용하지 않음
    @Column(nullable = false)
    private String nickname;

    // 로그인 상태, true: 로그인 상태, false: 로그아웃 상태
    @Column(nullable = false)
    private boolean isLoggedIn;

    //계정 생성 시간, @Column(updatable = false): 한번 저장된 후 수정 불가
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // 마지막 로그인 시간
    @Column
    private LocalDateTime lastLoginAt;

    //엔티티가 처음 생성될 때 자동으로 실행되는 메서드, 계정 생성 시간을 현재 시간으로 설정
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // 사용자의 닉네임을 업데이트하는 메서드, @param nickname 새로운 닉네임
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    // 로그인 처리 메서드, 로그인 상태를 true로 변경, 마지막 로그인 시간을 현재 시간으로 업데이트
    public void login() {
        this.isLoggedIn = true;
        this.lastLoginAt = LocalDateTime.now();
    }

    // 로그아웃 처리 메서드, 로그인 상태를 false로 변경
    public void logout() {
        this.isLoggedIn = false;
    }
}