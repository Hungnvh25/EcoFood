package com.example.ecofood.controller.OAuth;

import com.example.ecofood.auth.JwtUtil;
import com.example.ecofood.domain.User;
import com.example.ecofood.service.UserService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OAuth2Controller {

    UserService userService;
    @GetMapping("/oauth2/success")
    public String handleSuccess(Authentication authentication, HttpServletResponse response, HttpSession session) throws JOSEException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        String picture = oauthUser.getAttribute("picture");

        User user = this.userService.findByEmail(email);
        if (user == null) {
           user =  this.userService.createOAuth2User(email, name, picture);
        }

        // Sinh token JWT
        String token = JwtUtil.generateToken(user.getEmail());
        Cookie cookie = new Cookie("jwtToken", token);
        cookie.setMaxAge(21600);
        cookie.setPath("/");
        response.addCookie(cookie);

        // lưu currentUser
        User currentUser = this.userService.getCurrentUser();
        session.setAttribute("currentUser", currentUser);

        return "redirect:/";
    }
}
