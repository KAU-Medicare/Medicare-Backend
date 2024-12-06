package com.example.kaumedicare.Diary.controller;

import com.example.kaumedicare.Diary.dto.SubscriptionRequest;
import com.example.kaumedicare.Diary.model.PushSubscription;
import com.example.kaumedicare.Diary.repository.PushSubscriptionRepository;
import com.example.kaumedicare.Exception.EntityNotFoundException;
import com.example.kaumedicare.User.model.User;
import com.example.kaumedicare.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class PushController {
    private final PushSubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Value("${vapid.public.key}")
    private String publicKey;

    @GetMapping("/vapidPublicKey")
    public String getVapidPublicKey() {
        return publicKey;
    }

    @PostMapping("/subscribe/{kakaoId}")
    public ResponseEntity<Void> subscribe(
            @PathVariable String kakaoId,
            @RequestBody SubscriptionRequest request) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        PushSubscription subscription = new PushSubscription();
        subscription.setEndpoint(request.getEndpoint());
        subscription.setP256dh(request.getKeys().getP256dh());
        subscription.setAuth(request.getKeys().getAuth());
        subscription.setUser(user);

        subscriptionRepository.save(subscription);
        return ResponseEntity.ok().build();
    }
}