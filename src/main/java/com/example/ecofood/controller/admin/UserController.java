package com.example.ecofood.controller.admin;


import com.example.ecofood.domain.User;
import com.example.ecofood.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @GetMapping("/user")
    public String getUserDasBoard(Model model,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(required = false) String userName,
                                  @RequestParam(required = false) String email){

        Pageable pageable = PageRequest.of(page, 10);
        Page<User> users;


        if (userName != null || email != null) {
            users = this.userService.searchUsers(userName, email, pageable);
        } else {
            users = this.userService.getAllUsers(pageable);
        }

        // Thêm dữ liệu vào model
        model.addAttribute("users", users);
        model.addAttribute("totalUsers", this.userService.getTotalUsers());
        model.addAttribute("revenue", "$12,345");
        model.addAttribute("totalProducts", 567);

        return "admin/user/show";
    }
}
