package com.example.kaumedicare.User.service;

import com.example.kaumedicare.Exception.UserException;
import com.example.kaumedicare.User.dto.UserResponseDto;
import com.example.kaumedicare.User.dto.UserSaveRequestDto;
import com.example.kaumedicare.User.model.User;
import com.example.kaumedicare.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성
public class UserService {
    private final UserRepository userRepository;  // User 엔티티에 대한 데이터베이스 작업을 처리하는 리포지토리

    // 카카오 로그인 처리 메서드 (트랜잭션 처리)
    @Transactional
    public UserResponseDto loginWithKakao(UserSaveRequestDto requestDto) {
        // 카카오 ID로 기존 사용자 조회
        return userRepository.findById(requestDto.getKakaoId())
                .map(user -> {
                    // 기존 사용자인 경우 로그인 처리
                    user.login();
                    return UserResponseDto.from(user);
                })
                // 신규 사용자라면 회원가입 및 로그인 처리
                .orElseGet(() -> registerNewUser(requestDto));
    }

    // 신규 회원 등록 메서드
    private UserResponseDto registerNewUser(UserSaveRequestDto requestDto) {
        // User 엔티티 생성 및 초기화
        User newUser = requestDto.toEntity();
        newUser.login(); // 로그인 상태로 설정
        User savedUser = userRepository.save(newUser); // DB에 저장
        return UserResponseDto.from(savedUser); // DTO로 변환하여 반환
    }

    // 닉네임 변경
    @Transactional
    public UserResponseDto updateNickname(String kakaoId, String newNickname) {
        // 닉네임 유효성 검사
        if (newNickname == null || newNickname.trim().isEmpty()) {
            throw new UserException("닉네임은 비어 있을 수 없습니다.");
        }
        if (newNickname.length() > 20) {
            throw new UserException("닉네임은 20자를 초과할 수 없습니다.");
        }
        // 중복된 닉네임 여부 확인
        if (userRepository.findByNickname(newNickname).isPresent()) {
            throw new UserException("이미 사용 중인 닉네임입니다.");
        }

        // 사용자 조회, 없으면 예외 발생
        User user = userRepository.findById(kakaoId)
                .orElseThrow(() -> new UserException("사용자를 찾을 수 없습니다."));

        // 현재 닉네임과 같은 경우 처리
        if (user.getNickname().equals(newNickname)) {
            throw new UserException("현재 닉네임과 동일합니다.");
        }

        // 닉네임 업데이트
        user.updateNickname(newNickname);
        return UserResponseDto.from(user); // 업데이트된 사용자 정보 반환
    }

    // 닉네임으로 사용자 조회
    @Transactional(readOnly = true)
    public UserResponseDto findByNickname(String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new UserException("해당 닉네임을 가진 사용자를 찾을 수 없습니다: " + nickname));
        return UserResponseDto.from(user);
    }


    // 로그아웃 처리 메서드 (트랜잭션 처리)
    @Transactional
    public void logout(String kakaoId) {
        // 카카오 ID로 사용자 조회, 없으면 예외 발생
        User user = userRepository.findById(kakaoId)
                .orElseThrow(() -> new UserException("사용자를 찾을 수 없습니다."));
        // 로그아웃 처리
        user.logout();
    }

    // 기존 회원 여부 확인 메서드 (읽기 전용 트랜잭션)
    @Transactional(readOnly = true)
    public boolean isExistingUser(String kakaoId) {
        return userRepository.existsByKakaoId(kakaoId);
    }

    // ID로 사용자 조회 메서드 (읽기 전용 트랜잭션)
    @Transactional(readOnly = true)
    public UserResponseDto findById(String kakaoId) {
        // 카카오 ID로 사용자 조회, 없으면 예외 발생
        User user = userRepository.findById(kakaoId)
                .orElseThrow(() -> new UserException("해당 회원이 존재하지 않습니다. id=" + kakaoId));
        // 조회된 사용자 정보를 DTO로 변환하여 반환
        return UserResponseDto.from(user);
    }

    // 전체 회원 목록 조회 메서드 (읽기 전용 트랜잭션)
    @Transactional(readOnly = true)
    public List<UserResponseDto> findAllMembers() {
        // 모든 사용자를 조회하여 DTO로 변환 후 리스트로 반환
        return userRepository.findAll().stream()
                .map(UserResponseDto::from)
                .collect(Collectors.toList());
    }
}
