package com.example.ecofood.controller.client;


import com.example.ecofood.Util.RandomStringGenerator;
import com.example.ecofood.auth.JwtUtil;
import com.example.ecofood.DTO.LoginDTO;
import com.example.ecofood.DTO.UserDTO;
import com.example.ecofood.domain.User;
import com.example.ecofood.service.EmailService;
import com.example.ecofood.service.RecipeService;
import com.example.ecofood.service.RedisCacheService;
import com.example.ecofood.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HomePage {

    UserService userService;
    RecipeService recipeService;
    EmailService emailService;
    RandomStringGenerator randomStringGenerator;
    PasswordEncoder passwordEncoder;
    RedisCacheService redisCacheService;


    @GetMapping("/login")
    public String showLoginForm(Model model) {
        // kiem tra đã đăng nhập chưa
        if(this.userService.getCurrentUser() != null){
          return   "redirect:/";
        }

        model.addAttribute("loginDTO", new LoginDTO());
        return "client/login"; // Đảm bảo file nằm trong templates/client/login.html
    }


    @GetMapping("/register")
    public String getRegister(Model model) {
        // kiem tra đã đăng nhập chưa
        if(this.userService.getCurrentUser() != null){
            return   "redirect:/";
        }
        model.addAttribute("userDTO", new UserDTO());
        return "client/register";
    }



    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userDTO") UserDTO userDTO,
                               BindingResult result,
                               @RequestParam("confirmPassword") String confirmPassword) {
        if (result.hasErrors()) {
            return "client/register";
        }

        if (!userDTO.getPasswordHash().equals(confirmPassword)) {
            result.rejectValue("passwordHash", "error.user", "Mật khẩu không khớp");
            return "client/register";
        }

        if(this.userService.isEmailExist(userDTO.getEmail())){
            result.rejectValue("email", "error.user", "Email đã tồn tại");
            return "client/register";
        }

        this.userService.createUser(userDTO);
        this.emailService.sendRegistrationSuccessNotification(userDTO,confirmPassword);
        return "redirect:/login?success";
    }

    @PostMapping("/login")
    public String processLogin(Model model, @Valid @ModelAttribute("loginDTO") LoginDTO loginDTO,
                               BindingResult result, HttpServletResponse response, HttpSession session) {
        if (result.hasErrors()) {
            return "client/login";
        }

        boolean isValid = userService.authenticate(loginDTO.getEmail(), loginDTO.getPassword());
        if (!isValid) {
            result.reject("login.failed", "Tên đăng nhập hoặc mật khẩu không đúng");
            return "client/login";
        }
        try {
            // lưu token
            String token = JwtUtil.generateToken(loginDTO.getEmail());
            Cookie cookie = new Cookie("jwtToken", token);
            cookie.setMaxAge(21600); // 6h
            cookie.setPath("/");
            response.addCookie(cookie);

            // lưu currentUser
            User user = this.userService.getCurrentUser();
            session.setAttribute("currentUser", user);

            return "redirect:/";

        } catch (Exception e) {
            model.addAttribute("error", "Lỗi server khi tạo token!");
            return "client/login";
        }
    }

    @GetMapping("/logout")
    public String logoutUser() {
        return "redirect:/login";
    }

    @GetMapping("/403")
    public String get403() {
        return "403";
    }


    @GetMapping("/forgot-password")
    public String getForgotPassword(Model model) {
        // Kiểm tra đã đăng nhập chưa
        if (this.userService.getCurrentUser() != null) {
            return "redirect:/";
        }
        model.addAttribute("userDTO", new UserDTO());
        return "client/forgot-password";
    }

    @PostMapping("/forgot-password/send-otp")
    @ResponseBody
    public Map<String, Object> sendOtp(@RequestParam("email") String email) {
        Map<String, Object> response = new HashMap<>();

        if (!this.userService.isEmailExist(email)) {
            response.put("success", false);
            response.put("message", "Email không tồn tại.");
            return response;
        }

        String otp = this.randomStringGenerator.generateOtp();
        redisCacheService.set(email,otp,5L, TimeUnit.MINUTES);


        try {
            this.emailService.sendOtpEmail(email, otp);
            response.put("success", true);
            response.put("message", "OTP đã được gửi.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi gửi OTP. Vui lòng thử lại.");
        }

        return response;
    }

    @PostMapping("/forgot-password/verify-otp")
    @ResponseBody
    public Map<String, Object> verifyOtp(@RequestParam("otp") String otp,@RequestParam("email") String email) {

        Map<String, Object> response = new HashMap<>();

        String storedOtp = this.redisCacheService.get(email);


        if (storedOtp == null) {
            response.put("success", false);
            response.put("message", "Phiên làm việc hết hạn. Vui lòng thử lại.");
            return response;
        }

        if (!storedOtp.equals(otp)) {
            response.put("success", false);
            response.put("message", "Mã OTP không đúng.");
            return response;
        }

        response.put("success", true);
        return response;
    }

    @PostMapping("/forgot-password/reset-password")
    @ResponseBody
    public Map<String, Object> resetPassword(
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        Map<String, Object> response = new HashMap<>();

        if (email == null) {
            response.put("success", false);
            response.put("message", "Phiên làm việc hết hạn. Vui lòng thử lại.");
            return response;
        }

        if (!newPassword.equals(confirmPassword)) {
            response.put("success", false);
            response.put("message", "Mật khẩu xác nhận không khớp.");
            return response;
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        User user = this.userService.findByEmail(email);
        user.setPasswordHash(encodedPassword);
        this.userService.saveUser(user);

        System.out.println("Cập nhật mật khẩu mới cho " + email + ": " + encodedPassword);
        this.redisCacheService.delete(email);
        response.put("success", true);
        response.put("message", "Mật khẩu đã được đặt lại thành công!");

        return response;
    }

}

