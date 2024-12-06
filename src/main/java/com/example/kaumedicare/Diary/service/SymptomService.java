package com.example.kaumedicare.Diary.service;

import com.example.kaumedicare.Diary.dto.OccurredSymptomResponse;
import com.example.kaumedicare.Diary.dto.RecordSymptomRequest;
import com.example.kaumedicare.Diary.dto.SymptomResponse;
import com.example.kaumedicare.Diary.model.Diary;
import com.example.kaumedicare.Diary.model.OccurredSymptom;
import com.example.kaumedicare.Diary.model.Symptom;
import com.example.kaumedicare.Diary.repository.DiaryRepository;
import com.example.kaumedicare.Diary.repository.OccurredSymptomRepository;
import com.example.kaumedicare.Diary.repository.SymptomRepository;
import com.example.kaumedicare.Exception.EntityNotFoundException;
import com.example.kaumedicare.Exception.UnauthorizedException;
import com.example.kaumedicare.User.model.User;
import com.example.kaumedicare.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SymptomService {
    private final SymptomRepository symptomRepository;
    private final OccurredSymptomRepository occurredSymptomRepository;
    private final UserRepository userRepository;

    private final DiaryRepository diaryRepository;

    public List<SymptomResponse> getAllSymptoms() {
        return symptomRepository.findAll().stream()
                .map(symptom -> SymptomResponse.builder()
                        .id(symptom.getId())
                        .name(symptom.getName())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public OccurredSymptomResponse recordSymptom(RecordSymptomRequest request) {
        request.validate();

        User user = userRepository.findByKakaoId(request.getKakaoId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // symptomIds가 비어있는지 확인
        if (request.getSymptomIds() == null || request.getSymptomIds().isEmpty()) {
            throw new IllegalArgumentException("At least one symptom must be provided");
        }

        // 모든 증상이 실제로 존재하는지 확인
        List<Symptom> symptoms = symptomRepository.findAllById(request.getSymptomIds());
        if (symptoms.size() != request.getSymptomIds().size()) {
            throw new EntityNotFoundException("Some symptoms not found");
        }

        // 날짜에 해당하는 Diary 찾기 또는 생성
        Diary diary = diaryRepository.findByUserKakaoIdAndDate(user.getKakaoId(), request.getOccurredDate())
                .orElseGet(() -> {
                    Diary newDiary = Diary.builder()
                            .date(request.getOccurredDate())
                            .user(user)
                            .build();
                    return diaryRepository.save(newDiary);
                });

        OccurredSymptom occurredSymptom = OccurredSymptom.builder()
                .diary(diary)
                .symptoms(symptoms)
                .occurredDate(request.getOccurredDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .base64Image(request.getBase64Image())
                .build();

        return OccurredSymptomResponse.from(
                occurredSymptomRepository.save(occurredSymptom)
        );
    }

    public List<OccurredSymptomResponse> getSymptomsByPeriod(String kakaoId, LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("종료 날짜가 시작 날짜보다 빠를 수 없습니다.");
        }

        return occurredSymptomRepository
                .findByDiaryUserKakaoIdAndDiaryDateBetween(kakaoId, startDate, endDate)
                .stream()
                .map(OccurredSymptomResponse::from)
                .collect(Collectors.toList());
    }

    public List<OccurredSymptomResponse> getSymptomsByDate(String kakaoId, LocalDate date) {
        return occurredSymptomRepository
                .findByDiaryUserKakaoIdAndDiaryDate(kakaoId, date)
                .stream()
                .map(OccurredSymptomResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public OccurredSymptomResponse updateSymptom(Long id, RecordSymptomRequest request) {
        request.validate();
        OccurredSymptom occurredSymptom = occurredSymptomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("알레르기 기록을 찾을 수 없습니다."));

        List<Symptom> symptoms = symptomRepository.findAllById(request.getSymptomIds());

        occurredSymptom.updateSymptoms(symptoms);
        occurredSymptom.updateOccurredDate(request.getOccurredDate());
        occurredSymptom.updateStartTime(request.getStartTime());
        occurredSymptom.updateEndTime(request.getEndTime());
        occurredSymptom.updateImageUrl(request.getBase64Image());

        return OccurredSymptomResponse.from(occurredSymptom);
    }

    @Transactional
    public void deleteSymptom(String kakaoId, Long id) {
        // 해당 증상 기록 찾기
        OccurredSymptom occurredSymptom = occurredSymptomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("알레르기 기록을 찾을 수 없습니다."));

        // 권한 체크
        if (!occurredSymptom.getDiary().getUser().getKakaoId().equals(kakaoId)) {
            throw new UnauthorizedException("이 알레르기 기록을 삭제할 권한이 없습니다.");
        }

        // 연관된 diary 가져오기
        Diary diary = occurredSymptom.getDiary();
        Long diaryId = diary.getId();

        // 증상 기록 삭제
        occurredSymptomRepository.deleteById(id);

        // 해당 diary에 남은 증상 기록이 있는지 확인
        long remainingSymptoms = occurredSymptomRepository
                .countByDiaryId(diaryId);

        // 남은 증상 기록이 없으면 diary도 삭제
        if (remainingSymptoms == 0) {
            diaryRepository.deleteById(diaryId);
        }
    }
}