package com.example.ecofood.controller.client;

import com.example.ecofood.domain.Notification;
import com.example.ecofood.domain.Recipe;
import com.example.ecofood.domain.User;
import com.example.ecofood.service.NotificationService;
import com.example.ecofood.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {

    NotificationService notificationService;
    UserService userService;

    @GetMapping("/notification")
    public String getPageNotification(Model model){

        User  user = this.userService.getCurrentUser();
        List<Notification> notifications = this.notificationService.findNotificationsByEmail(user.getEmail());


        model.addAttribute("notifications", notifications);

        return "client/Notification/show";
    }
}
