package com.example.kaumedicare.KakaoLogin.controller;

import com.example.kaumedicare.KakaoLogin.service.LoginService;
import com.example.kaumedicare.User.dto.UserResponseDto;
import com.example.kaumedicare.User.dto.UserSaveRequestDto;
import com.example.kaumedicare.Exception.UserException;
import com.example.kaumedicare.User.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequiredArgsConstructor    // @Autowired 대신 생성자 주입 방식 사용
public class LoginController {
    private final LoginService loginService;
    private final UserService userService;    // UserService 추가

    @GetMapping("/api/kakaologin/{code}")
    public ResponseEntity<UserResponseDto> kakaoLogin(@PathVariable("code") String code) {
        try {
            // 1. 카카오 액세스 토큰 받기
            String kakaoToken = loginService.requestToken(code);

            // 2. 카카오 사용자 정보 받기
            HashMap<String, String> userInfo = loginService.requestUser(kakaoToken);

            // 3. 회원가입 또는 로그인 처리
            UserSaveRequestDto requestDto = UserSaveRequestDto.builder()
                    .kakaoId(userInfo.get("id"))
                    .nickname(userInfo.get("nickname"))
                    .build();

            // 4. UserService의 loginWithKakao 메서드 호출
            UserResponseDto responseDto = userService.loginWithKakao(requestDto);

            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            throw new UserException("카카오 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}