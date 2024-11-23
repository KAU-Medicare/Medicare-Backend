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
        LocalDate date = request.getOccurredDateTime().toLocalDate();
        Diary diary = diaryRepository.findByUserKakaoIdAndDate(user.getKakaoId(), date)
                .orElseGet(() -> {
                    Diary newDiary = Diary.builder()
                            .date(date)
                            .user(user)
                            .build();
                    return diaryRepository.save(newDiary);
                });

        // OccurredSymptom 생성 및 저장
        OccurredSymptom occurredSymptom = OccurredSymptom.builder()
                .diary(diary)
                .symptoms(symptoms)  // 여기서 symptoms가 비어있지 않은지 확인
                .occurredDateTime(request.getOccurredDateTime())
                .base64Image(request.getBase64Image())
                .build();

        return OccurredSymptomResponse.from(
                occurredSymptomRepository.save(occurredSymptom)
        );
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
        OccurredSymptom occurredSymptom = occurredSymptomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("알레르기 기록을 찾을 수 없습니다."));

        List<Symptom> symptoms = symptomRepository.findAllById(request.getSymptomIds());

        occurredSymptom.updateSymptoms(symptoms);
        occurredSymptom.updateOccurredDateTime(request.getOccurredDateTime());
        occurredSymptom.updateImageUrl(request.getBase64Image());

        return OccurredSymptomResponse.from(occurredSymptom);
    }

    @Transactional
    public void deleteSymptom(Long id) {
        occurredSymptomRepository.deleteById(id);
    }
}