/*
package com.example.kaumedicare.Diary.service;


import com.example.kaumedicare.Diary.dto.*;
import com.example.kaumedicare.Diary.model.Diary;
import com.example.kaumedicare.Diary.model.TakenHealthFood;
import com.example.kaumedicare.Diary.model.TakenMedicine;
import com.example.kaumedicare.Diary.repository.DiaryRepository;
import com.example.kaumedicare.Diary.repository.TakenHealthFoodRepository;
import com.example.kaumedicare.Diary.repository.TakenMedicineRepository;
import com.example.kaumedicare.Dur.repository.DurRepository;
import com.example.kaumedicare.HealthFood.model.HealthFood;
import com.example.kaumedicare.HealthFood.repository.HealthFoodRepository;
import com.example.kaumedicare.Medicine.model.Medicine;
import com.example.kaumedicare.Medicine.repository.MedicineRepository;
import com.example.kaumedicare.User.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository medicineDiaryRepository;
    private final MedicineRepository medicineRepository;
    private final HealthFoodRepository healthFoodRepository;
    private final TakenMedicineRepository takenMedicineRepository;
    private final TakenHealthFoodRepository takenHealthFoodRepository;
    private final DurRepository durRepository;

    @Transactional
    public IdResponse createDiary(User user, DiaryRequest request) {
        LocalDate diaryDate = request.getDate();
        LocalDate today = LocalDate.now();

        if (diaryDate.isAfter(today)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "미래 날짜에 일지를 생성할 수 없습니다.");
        }

        if (medicineDiaryRepository.existsByUserKakaoIdAndDate(user.getKakaoId(), diaryDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 해당 날짜에 일지가 존재합니다.");
        }

        Diary diary = Diary.builder()
                .date(diaryDate)
                .user(user)
                .build();

        Diary savedDiary = medicineDiaryRepository.save(diary);
        return new IdResponse(savedDiary.getId());
    }

    @Transactional
    public IdResponse addDiaryElement(User user, Long diaryId, DiaryElementCreateRequest request) {
        Diary diary = getDiaryAndValidateUser(diaryId, user);
        validateDateTime(diary.getDate(), request.getDateTime());

        switch (request.getElementType()) {
            case MEDICINE -> {
                Medicine medicine = medicineRepository.findById(request.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 약품입니다."));

                // DUR 체크
                List<TakenMedicine> existingMedicines = diary.getTakenMedicines();
                for (TakenMedicine taken : existingMedicines) {
                    if (durRepository.existsByTargetMedicineAndDurMedicine(taken.getMedicine(), medicine) ||
                            durRepository.existsByTargetMedicineAndDurMedicine(medicine, taken.getMedicine())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "병용금기 약품입니다: " + taken.getMedicine().getItemName() + " 와(과) " + medicine.getItemName());
                    }
                }

                TakenMedicine takenMedicine = TakenMedicine.builder()
                        .takenDateTime(request.getDateTime())
                        .medicine(medicine)
                        .medicineDiary(diary)
                        .build();
                takenMedicineRepository.save(takenMedicine);
            }
            case HEALTH_FOOD -> {
                HealthFood healthFood = healthFoodRepository.findById(request.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 영양제입니다."));

                TakenHealthFood takenHealthFood = TakenHealthFood.builder()
                        .takenDateTime(request.getDateTime())
                        .healthFood(healthFood)
                        .medicineDiary(diary)
                        .build();
                takenHealthFoodRepository.save(takenHealthFood);
            }
        }

        return new IdResponse(diaryId);
    }

    @Transactional
    public IdResponse deleteDiaryElement(User user, Long diaryId, DiaryElementDeleteRequest request) {
        Diary diary = getDiaryAndValidateUser(diaryId, user);

        switch (request.getElementType()) {
            case MEDICINE -> takenMedicineRepository.deleteByMedicineId(request.getId());
            case HEALTH_FOOD -> takenHealthFoodRepository.deleteByHealthFoodId(request.getId());
        }

        return new IdResponse(diaryId);
    }

    public DiaryResponse getDiary(User user, LocalDate date) {
        Diary diary = medicineDiaryRepository.findByUserKakaoIdAndDate(user.getKakaoId(), date)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 날짜의 일지를 찾을 수 없습니다."));

        return createDiaryResponse(diary);
    }

    public List<DiaryResponse> getDiaryPeriod(User user, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "시작 날짜가 종료 날짜보다 이후일 수 없습니다.");
        }

        return medicineDiaryRepository
                .findAllByUserKakaoIdAndDateBetweenOrderByDateDesc(user.getKakaoId(), startDate, endDate)
                .stream()
                .map(this::createDiaryResponse)
                .collect(Collectors.toList());
    }

    private Diary getDiaryAndValidateUser(Long diaryId, User user) {
        Diary diary = medicineDiaryRepository.findById(diaryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 일지입니다."));

        if (!diary.getUser().getKakaoId().equals(user.getKakaoId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }

        return diary;
    }

    private void validateDateTime(LocalDate diaryDate, LocalDateTime dateTime) {
        LocalDateTime startOfDay = diaryDate.atStartOfDay();
        LocalDateTime endOfDay = diaryDate.plusDays(1).atStartOfDay();

        if (dateTime.isBefore(startOfDay) || !dateTime.isBefore(endOfDay)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "선택한 일자의 시간만 등록할 수 있습니다.");
        }

        if (dateTime.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "미래 시간으로는 등록할 수 없습니다.");
        }
    }

    private DiaryResponse createDiaryResponse(Diary diary) {
        DiaryResponse response = new DiaryResponse();
        response.setId(diary.getId());
        response.setDate(diary.getDate());
        response.setTakenMedicines(diary.getTakenMedicines());
        response.setTakenHealthFoods(diary.getTakenHealthFoods());
        return response;
    }
}

 */