package com.example.ecofood.controller.client;

import com.example.ecofood.Util.RandomStringGenerator;
import com.example.ecofood.domain.Category;
import com.example.ecofood.domain.Recipe;
import com.example.ecofood.domain.User;
import com.example.ecofood.domain.UserSetting;
import com.example.ecofood.service.ImageService;
import com.example.ecofood.service.NotificationService;
import com.example.ecofood.service.RecipeService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Random;

@Controller("clientUserController")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;
    ImageService imageService;
    PasswordEncoder passwordEncoder;
    RandomStringGenerator randomStringGenerator;
    RecipeService recipeService;
    NotificationService notificationService;

    @PostMapping("/profile/update")
    public String updateProfile(Model model,
                                @RequestParam("userName") String userName,
                                @RequestParam(value = "voiceGender", required = false) String voiceGender,
                                @RequestParam(value = "region", required = false) String region,
                                @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                                HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes
            ) throws IOException {
        String refererUrl = request.getHeader("Referer");

        try{
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

            session.invalidate();
            session = request.getSession(true);
            session.setAttribute("currentUser", user);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thành công");
            // Cập nhật model
            model.addAttribute("currentUser", user);


            return "redirect:"+ refererUrl;
        }catch (Exception ex){
            redirectAttributes.addFlashAttribute("failed", "Có lỗi khi cập nhật: " + ex.getMessage());
            return "redirect:"+ refererUrl;
        }
    }



    @PostMapping("/profile/update-password")
    public String updatePassword(@RequestParam(value = "currentPassword", required = false) String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpServletRequest request,RedirectAttributes redirectAttributes) {
        String refererUrl = request.getHeader("Referer");

        try {
            if (this.userService.isNewPassWord()){
                this.userService.setPassWordNewUser(confirmPassword);
                redirectAttributes.addFlashAttribute("success", "Cập nhật mật khẩu thành công");
                return "redirect:" + refererUrl;
            } else {
                User user = this.userService.getCurrentUser();
                if (this.passwordEncoder.matches(currentPassword, user.getPasswordHash()) && newPassword.equals(confirmPassword)){
                    this.userService.setPassWordNewUser(confirmPassword);
                    redirectAttributes.addFlashAttribute("success", "Cập nhật mật khẩu thành công");
                    return "redirect:" + refererUrl;
                }
            }
            redirectAttributes.addFlashAttribute("failed", "Có lỗi khi cập nhật  mật khẩu: " );
            return "redirect:" + refererUrl;
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("failed", "Có lỗi khi cập nhật  mật khẩu: " + e.getMessage());
            return "redirect:" + refererUrl;
        }
    }


    @PostMapping("/profile/delete")
    public String deleteUser() {

        User user = this.userService.getCurrentUser();
        user.getUserSetting().setUrlImage("");
        user.setPasswordHash(this.passwordEncoder.encode(this.randomStringGenerator.generateRandomString(10)));
        user.setEmail(this.randomStringGenerator.generateRandomString(10)+"@gmail.com");

        List<Recipe> recipes = this.recipeService.getAllRecipes();
        User userAdmin = this.userService.findByEmail("ecofood.2025@gmail.com");
        for (Recipe recipe:recipes
             ) {
            if (recipe.getIsPendingRecipe() == Boolean.FALSE){
                recipe.setUser(userAdmin);
                this.recipeService.saveRecipe(recipe);
            }
        }
        this.userService.saveUser(user);

        return "redirect:/logout";
    }


    @PostMapping("/feedback")
    public String feedback(HttpServletRequest request, @RequestParam("feedbackTitle") String feedbackTitle,
                           @RequestParam("feedbackContent") String feedbackContent,RedirectAttributes redirectAttributes) {
        String refererUrl = request.getHeader("Referer");

        try {
            User user = this.userService.getCurrentUser();
            this.notificationService.createFeedbackNotification(user.getUserName(),feedbackTitle,feedbackContent);

            redirectAttributes.addFlashAttribute("success", "Bạn đã gửi thành công");
            return "redirect:" + refererUrl;
        }catch (Exception ex){
            redirectAttributes.addFlashAttribute("failed", "Gửi thất bại : "+ex.getMessage());
            return "redirect:" + refererUrl;
        }
    }


}



