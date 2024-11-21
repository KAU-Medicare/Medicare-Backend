package com.example.kaumedicare.Diary.service;

import com.example.kaumedicare.Diary.dto.OccurredSymptomResponse;
import com.example.kaumedicare.Diary.dto.RecordSymptomRequest;
import com.example.kaumedicare.Diary.dto.SymptomResponse;
import com.example.kaumedicare.Diary.model.Diary;
import com.example.kaumedicare.Diary.model.OccurredSymptom;
import com.example.kaumedicare.Diary.model.Symptom;
import com.example.kaumedicare.Diary.repository.OccurredSymptomRepository;
import com.example.kaumedicare.Diary.repository.SymptomRepository;
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
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Symptom> symptoms = symptomRepository.findAllById(request.getSymptomIds());

        Diary diary = Diary.builder()
                .date(request.getOccurredDateTime().toLocalDate())
                .user(user)
                .build();

        OccurredSymptom occurredSymptom = OccurredSymptom.builder()
                .diary(diary)
                .symptoms(symptoms)  // 여러 증상 한 번에 설정
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