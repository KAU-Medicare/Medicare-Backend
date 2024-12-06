package com.example.kaumedicare.Diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequest {
    private String endpoint;
    private Keys keys;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Keys {
        private String p256dh;
        private String auth;
    }
}