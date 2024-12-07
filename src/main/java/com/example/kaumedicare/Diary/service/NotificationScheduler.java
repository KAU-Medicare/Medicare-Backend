package com.example.kaumedicare.Diary.service;

import com.example.kaumedicare.Diary.dto.MedicineType;
import com.example.kaumedicare.Diary.model.Inventory;
import com.example.kaumedicare.Diary.model.PushSubscription;
import com.example.kaumedicare.Diary.repository.InventoryRepository;
import com.example.kaumedicare.Diary.repository.PushSubscriptionRepository;
import com.example.kaumedicare.User.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.kaumedicare.Diary.dto.InventoryResponse.getItemNameSafely;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {
    private final PushService pushService;
    private final InventoryRepository inventoryRepository;
    private final PushSubscriptionRepository subscriptionRepository;

    @Scheduled(cron = "0 * * * * *") // 매분 실행
    public void checkAndSendNotifications() {
        log.info("Checking notifications at: {}", LocalDateTime.now());
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();
        DayOfWeek currentDay = today.getDayOfWeek();

        // useNotification이 true인 약들 중에서 현재 시간에 알림이 필요한 것들 조회
        List<Inventory> inventoriesToNotify = inventoryRepository
                .findByUseNotificationTrueAndTakingTimeAndTakingDaysContaining(now, currentDay);

        log.info("Found {} notifications to send", inventoriesToNotify.size());

        for (Inventory inventory : inventoriesToNotify) {
            User user = inventory.getUser();
            List<PushSubscription> subscriptions = subscriptionRepository.findByUser(user);

            for (PushSubscription subscription : subscriptions) {
                try {
                    String payload = createNotificationPayload(inventory);

                    Subscription sub = new Subscription(
                            subscription.getEndpoint(),
                            new Subscription.Keys(subscription.getP256dh(), subscription.getAuth())
                    );

                    pushService.send(new Notification(sub, payload));
                    log.info("Notification sent for user: {}, medicine: {}",
                            user.getKakaoId(), inventory.getNickname());
                } catch (Exception e) {
                    log.error("Failed to send notification", e);
                }
            }
        }
    }

    private String createNotificationPayload(Inventory inventory) throws JsonProcessingException {
        String itemName = inventory.getNickname();
        if (itemName == null) {
            itemName = inventory.getType() == MedicineType.MEDICINE ?
                    inventory.getMedicine().getItemName() :
                    inventory.getHealthFood().getProduct();
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("title", inventory.getType() == MedicineType.MEDICINE ? "복약 알림" : "영양제 알림");
        payload.put("body", String.format("%s 복용 시간입니다.", itemName));

        return new ObjectMapper().writeValueAsString(payload);
    }
}