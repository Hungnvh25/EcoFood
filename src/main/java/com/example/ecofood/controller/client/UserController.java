package com.example.ecofood.controller.client;

import com.example.ecofood.domain.Category;
import com.example.ecofood.domain.User;
import com.example.ecofood.domain.UserSetting;
import com.example.ecofood.service.ImageService;
import com.example.ecofood.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@Controller("clientUserController")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;
    ImageService imageService;
    PasswordEncoder passwordEncoder;
    @PostMapping("/profile/update")
    public String updateProfile(Model model,
                                @RequestParam("userName") String userName,
                                @RequestParam(value = "voiceGender", required = false) String voiceGender,
                                @RequestParam(value = "region", required = false) String region,
                                @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                                HttpSession session, HttpServletRequest request
            ) throws IOException {

        UserSetting.Gender gender = UserSetting.Gender.valueOf(voiceGender);

        User user = this.userService.getCurrentUser();
        user.setUserName(userName);
        user.getUserSetting().setVoiceGender(gender);
        user.getUserSetting().setRegion(Category.Region.valueOf(region));

        user.getUserSetting().setAccent(this.userService.setAccent(UserSetting.Gender.valueOf(voiceGender),UserSetting.Region.valueOf(region)));

        if (!profileImage.isEmpty()) {

            String urlImage = this.imageService.saveImage(profileImage,"Avatars");

            user.getUserSetting().setUrlImage(urlImage);
        }
        this.userService.saveUser(user);

        // Xóa session cũ và tạo lại session với user mới
        session.invalidate();  // Hủy session hiện tại
        session = request.getSession(true);  // Tạo session mới
        session.setAttribute("currentUser", user);  // Lưu thông tin người dùng mới vào session

        // Cập nhật model
        model.addAttribute("currentUser", user);


        return "redirect:/";
    }


    @PostMapping("/profile/update-password")
    public String updatePassword(@RequestParam(value = "currentPassword",required = false) String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session) {
        if (this.userService.isNewPassWord()){
            this.userService.setPassWordNewUser(confirmPassword);
            return "redirect:/";
        }else {
            User user = this.userService.getCurrentUser();
            if (this.passwordEncoder.matches(currentPassword,user.getPasswordHash()) && newPassword.equals(confirmPassword)){
                this.userService.setPassWordNewUser(confirmPassword);
                return "redirect:/";
            }
        }

        return "redirect:/";
    }

}



