package com.example.kaumedicare.Diary.repository;

import com.example.kaumedicare.Diary.model.PushSubscription;
import com.example.kaumedicare.User.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    List<PushSubscription> findByUser(User user);
}