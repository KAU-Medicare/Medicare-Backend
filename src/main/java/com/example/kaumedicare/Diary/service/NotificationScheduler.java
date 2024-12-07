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

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {
    private final PushService pushService;
    private final InventoryRepository inventoryRepository;
    private final PushSubscriptionRepository subscriptionRepository;

    @Scheduled(cron = "0 * * * * *")
    public void checkAndSendNotifications() {
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        LocalTime now = LocalTime.now(zoneId).withSecond(0).withNano(0);
        String currentTime = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        LocalDate today = LocalDate.now(zoneId);
        DayOfWeek currentDay = today.getDayOfWeek();

        log.info("Checking notifications - Current Time: {}, Day: {}",
                currentTime, currentDay);

        List<Inventory> allNotifications = inventoryRepository.findByUseNotificationTrue();
        allNotifications.forEach(inv -> {
            log.info("Stored notification - Time: {}, Current time: {}, Equal: {}",
                    inv.getTakingTime(),
                    currentTime,
                    inv.getTakingTime().equals(currentTime));
        });

        List<Inventory> inventoriesToNotify = inventoryRepository
                .findByUseNotificationTrueAndTakingTimeAndTakingDaysContaining(
                        currentTime,
                        currentDay);

        log.info("Found {} notifications to send", inventoriesToNotify.size());

        if (!inventoriesToNotify.isEmpty()) {
            inventoriesToNotify.forEach(inv -> {
                log.info("Notification details - User: {}, Medicine: {}, Time: {}, Days: {}",
                        inv.getUser().getKakaoId(),
                        inv.getNickname() != null ? inv.getNickname() :
                                (inv.getType() == MedicineType.MEDICINE ?
                                        inv.getMedicine().getItemName() :
                                        inv.getHealthFood().getProduct()),
                        inv.getTakingTime(),
                        inv.getTakingDays());
            });
        }

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
                            user.getKakaoId(),
                            inventory.getNickname() != null ? inventory.getNickname() :
                                    (inventory.getType() == MedicineType.MEDICINE ?
                                            inventory.getMedicine().getItemName() :
                                            inventory.getHealthFood().getProduct()));
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