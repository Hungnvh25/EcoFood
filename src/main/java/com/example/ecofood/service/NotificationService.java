package com.example.ecofood.service;

import com.example.ecofood.domain.Notification;
import com.example.ecofood.domain.Recipe;
import com.example.ecofood.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    @Transactional
    public void createRecipeStatusNotification(Recipe recipe, boolean isApproved) {
        if (recipe == null || recipe.getUser() == null) {
            return;
        }

        String title = isApproved ? "Công thức được phê duyệt" : "Công thức bị từ chối";
        String content = isApproved
                ? String.format("Công thức '%s' của bạn đã được phê duyệt bởi admin.", recipe.getTitle())
                : String.format("Công thức '%s' của bạn đã bị từ chối bởi admin.", recipe.getTitle());

        Notification notification = Notification.builder()
                .title(title)
                .content(content)
                .user(recipe.getUser())
                .createdDate(LocalDate.now())
                .build();

        this.notificationRepository.save(notification);
    }

}
