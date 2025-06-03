package com.example.ecofood.controller.admin;

import com.example.ecofood.domain.User;
import com.example.ecofood.service.ImageService;
import com.example.ecofood.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardController {

    UserService userService;
    ImageService imageService;

    @GetMapping("")
    public String getDashBoard(Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            User user = (User) auth.getPrincipal();
            model.addAttribute("user", user);
        }

        return "admin/dashboard";
    }

    @PostMapping("/update-user")
    public String updateUser(@ModelAttribute User user,
                             @RequestParam("profileImage") MultipartFile profileImage) throws IOException {

        user = this.userService.findUserByUserName(user.getUserName());

        String fileName = this.imageService.saveImage(profileImage, "uploads");

        user.getUserSetting().setUrlImage(fileName);
        this.userService.saveUser(user);


        return "redirect:/admin";
    }
}
