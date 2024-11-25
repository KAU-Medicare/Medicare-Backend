package com.example.kaumedicare.StandardCode.service;

import com.example.kaumedicare.Exception.StandardCodeNotFoundException;
import com.example.kaumedicare.OpenAPI.service.StandardCodeApiService;
import com.example.kaumedicare.StandardCode.model.StandardCode;
import com.example.kaumedicare.StandardCode.repository.StandardCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class StandardCodeService {

    private final StandardCodeRepository standardCodeRepository;
    private final StandardCodeApiService standardCodeApiService;

    @Transactional(readOnly = true)
    public String findItemSeqByStandardCode(String standardCode) {
        return standardCodeRepository.findByStandardCode(standardCode)
                .map(StandardCode::getItemSeq)
                .orElseThrow(() -> new StandardCodeNotFoundException(
                        String.format("표준코드 %s에 해당하는 품목기준코드를 찾을 수 없습니다.", standardCode)));
    }

    public void updateStandardCodes() {
        standardCodeApiService.fetchAndSaveAllStandardCodes()
                .thenRun(() -> log.info("표준코드 업데이트가 성공적으로 완료되었습니다."))
                .exceptionally(throwable -> {
                    log.error("표준코드 업데이트 중 오류 발생", throwable);
                    return null;
                });
    }
}