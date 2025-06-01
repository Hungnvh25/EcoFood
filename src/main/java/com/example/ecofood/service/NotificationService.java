package com.example.ecofood.service;

import com.example.ecofood.domain.Notification;
import com.example.ecofood.repository.NotificationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {

    NotificationRepository notificationRepository;

    public List<Notification> findNotificationsByEmail(String email){

        List<Notification> notificationList = this.notificationRepository.findByUserEmail(email);

        return notificationList;
    }

}
