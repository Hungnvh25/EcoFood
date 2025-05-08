package com.example.ecofood.Util;

import com.example.ecofood.domain.User;
import com.example.ecofood.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttribute {

    @Autowired
    private UserService userService;

    @ModelAttribute
    public void addAttributes(Model model, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");

        if (user == null) {
            user = this.userService.getCurrentUser(); // hoặc từ token/session
            session.setAttribute("currentUser", user);
        }

        model.addAttribute("currentUser", user);
    }
}
