package com.example.ecofood.controller.admin;


import com.example.ecofood.domain.DTO.LoginDTO;
import com.example.ecofood.domain.DTO.UserDTO;
import com.example.ecofood.domain.User;
import com.example.ecofood.service.ImageService;
import com.example.ecofood.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;
    ImageService imageService;

    @GetMapping("/user")
    public String getUserDasBoard(Model model,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(required = false) String userName,
                                  @RequestParam(required = false) String email){

        Pageable pageable = PageRequest.of(page, 10);
        Page<User> users;


        if (userName != null && !userName.isEmpty() || email != null && !email.isEmpty()) {
            users = this.userService.searchUsers(userName, email, pageable);
        } else {
            users = this.userService.getAllUsers(pageable);
        }

        // Thêm dữ liệu vào model
        model.addAttribute("users", users);
        model.addAttribute("totalUsers", this.userService.getTotalUsers());
        model.addAttribute("revenue", "$12,345");
        model.addAttribute("totalProducts", 567);

        if (!model.containsAttribute("editUser")) {
            model.addAttribute("editUser", new User());
        }
        return "admin/user/show";
    }


    @PostMapping("/user/update")
    public String updateUser(@Valid @ModelAttribute("editUser") User user, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            result.getAllErrors().forEach(error -> {
                errorMessage.append(error.getDefaultMessage()).append("; ");
            });
            redirectAttributes.addFlashAttribute("error", errorMessage.toString());
            return "redirect:/admin/user";
        }

        // Cập nhật user
        this.userService.saveUser(user);
        redirectAttributes.addFlashAttribute("success", "User updated successfully.");
        return "redirect:/admin/user";
    }

    @PostMapping("/user/delete")
    public String deleteUser(@RequestParam("id") String id, RedirectAttributes redirectAttributes) {
        try {
            if (id == null) {
                redirectAttributes.addFlashAttribute("error", "User ID is required.");
                return "redirect:/admin/user";
            }
            this.userService.deleteUser(Long.parseLong(id));
            redirectAttributes.addFlashAttribute("success", "User deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete user: " + e.getMessage());
        }
        return "redirect:/admin/user";
    }


    @GetMapping("/user/create")
    public String showAddUserForm(Model model) {
        User user = this.userService.getCurrentUser();
        model.addAttribute("newUser", new User());
        model.addAttribute("user", user);

        return "admin/user/addUser";
    }

    // API để xử lý thêm user
    @PostMapping("/user/create")
    public String addUser(@Valid @ModelAttribute("newUser") User user, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/user/addUser";
        }

        UserDTO userDTO = UserDTO.builder()
                .userName(user.getUserName())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .joinDate(user.getJoinDate())
                .build();
        // Lưu user mới
        this.userService.createUser(userDTO);
        redirectAttributes.addFlashAttribute("success", "User added successfully.");
        return "redirect:/admin/user";
    }
}
