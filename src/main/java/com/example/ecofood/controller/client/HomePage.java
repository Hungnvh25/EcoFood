package com.example.ecofood.controller.client;


import com.example.ecofood.auth.JwtUtil;
import com.example.ecofood.domain.DTO.LoginDTO;
import com.example.ecofood.domain.DTO.RecipeDTO;
import com.example.ecofood.domain.DTO.UserDTO;
import com.example.ecofood.domain.User;
import com.example.ecofood.service.RecipeService;
import com.example.ecofood.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HomePage {

    UserService userService;
    RecipeService recipeService;

    @GetMapping ("/")
    public String getEcoFood(Model model, HttpServletRequest request,HttpSession session){
        // lưu currentUser
        User user = this.userService.getCurrentUser();
        session.setAttribute("currentUser", user);

        // Lấy danh sách recipes từ service
        List<RecipeDTO> recipes = this.recipeService.getAllRecipes();
        model.addAttribute("recipes", recipes);

        return "index";
    }


    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginDTO", new LoginDTO());
        return "client/login"; // Đảm bảo file nằm trong templates/client/login.html
    }


    @GetMapping("/register")
    public String getRegister(Model model) {
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

        this.userService.createUser(userDTO);
        return "redirect:/login?success";
    }

    @PostMapping("/login")
    public String processLogin(Model model, @Valid @ModelAttribute("loginDTO") LoginDTO loginDTO,
                               BindingResult result, HttpServletResponse response) {
        if (result.hasErrors()) {
            return "client/login";
        }

        boolean isValid = userService.authenticate(loginDTO.getUsername(), loginDTO.getPassword());
        if (!isValid) {
            result.reject("login.failed", "Tên đăng nhập hoặc mật khẩu không đúng");
            return "client/login";
        }
        try {
            // lưu token
            String token = JwtUtil.generateToken(loginDTO.getUsername());
            Cookie cookie = new Cookie("jwtToken", token);
            cookie.setMaxAge(3600);
            cookie.setPath("/");
            response.addCookie(cookie);

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


}

