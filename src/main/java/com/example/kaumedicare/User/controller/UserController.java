package com.example.kaumedicare.User.controller;

import com.example.kaumedicare.KakaoLogin.service.LoginService;
import com.example.kaumedicare.User.dto.UpdateNicknameRequest;
import com.example.kaumedicare.User.dto.UserResponseDto;
import com.example.kaumedicare.User.dto.UserSaveRequestDto;
import com.example.kaumedicare.Exception.UserException;
import com.example.kaumedicare.User.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final LoginService loginService;

    // 카카오 로그인
    @PostMapping("/kakao/{code}")
    public ResponseEntity<UserResponseDto> kakaoLogin(@PathVariable String code) {
        try {
            String kakaoToken = loginService.requestToken(code);
            HashMap<String, String> userInfo = loginService.requestUser(kakaoToken);

            UserSaveRequestDto requestDto = UserSaveRequestDto.builder()
                    .kakaoId(userInfo.get("id"))
                    .nickname(userInfo.get("nickname"))
                    .build();

            UserResponseDto responseDto = userService.loginWithKakao(requestDto);
            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            throw new UserException("카카오 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 닉네임 변경
    @PutMapping("/{kakaoId}/nickname")
    public ResponseEntity<UserResponseDto> updateNickname(
            @PathVariable String kakaoId,
            @RequestBody UpdateNicknameRequest request) {
        try {
            UserResponseDto updatedUser = userService.updateNickname(kakaoId, request.getNewNickname());
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            throw new UserException("닉네임 변경 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 로그아웃
    @PostMapping("/logout/{kakaoId}")
    public ResponseEntity<Void> logout(@PathVariable String kakaoId) {
        try {
            userService.logout(kakaoId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new UserException("로그아웃 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 회원 존재 여부 확인
    @GetMapping("/check/{kakaoId}")
    public ResponseEntity<Boolean> checkExistingUser(@PathVariable String kakaoId) {
        return ResponseEntity.ok(userService.isExistingUser(kakaoId));
    }

    // 회원 정보 조회
    @GetMapping("/{kakaoId}")
    public ResponseEntity<UserResponseDto> findById(@PathVariable String kakaoId) {
        return ResponseEntity.ok(userService.findById(kakaoId));
    }

    // 전체 회원 조회
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> findAll() {
        return ResponseEntity.ok(userService.findAllMembers());
    }
}