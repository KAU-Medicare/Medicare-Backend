package com.example.kaumedicare.Exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//모든 예외를 핸들링하는 전역 예외 처리기
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * UserException 처리
     *
     * @param e UserException
     * @return 에러 응답
     */
    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorResponse> handleUserException(UserException e) {
        log.error("UserException 발생: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
    }

    /**
     * 그 외 예상치 못한 예외 처리
     *
     * @param e Exception
     * @return 에러 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("예상치 못한 예외 발생: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("서버 내부 오류가 발생했습니다."));
    }

    /**
     * 에러 응답을 위한 내부 클래스
     */
    @Getter
    @AllArgsConstructor
    static class ErrorResponse {
        private final String message;
    }
}