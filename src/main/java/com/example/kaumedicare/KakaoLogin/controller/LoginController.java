package com.example.kaumedicare.KakaoLogin.controller;

import com.example.kaumedicare.KakaoLogin.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
public class LoginController {

    @Autowired
    private LoginService loginService;

    // 코드를 이용해 access token 을 얻어다 줄 것임
    @GetMapping("/api/kakaologin/{code}")
    public HashMap<String, String> kakaoLogin(@PathVariable("code") String code) {

        System.out.println(code);

        // 토큰을 요청하여 얻음
        String kakaoToken = loginService.requestToken(code);
        System.out.println("카카오토큰" + kakaoToken);

        // 사용자 정보를 요청하여 얻음
        HashMap<String, String> userInfo = loginService.requestUser(kakaoToken);
        System.out.println("userInfo : " + userInfo);
        return userInfo;
    }
}
