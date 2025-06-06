package com.example.ecofood.controller.client;

import com.example.ecofood.domain.CookSnap;
import com.example.ecofood.domain.Recipe;
import com.example.ecofood.domain.User;
import com.example.ecofood.service.CookSnapService;
import com.example.ecofood.service.ImageService;
import com.example.ecofood.service.RecipeService;
import com.example.ecofood.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/cook-snap")
public class CookSnapController {


     CookSnapService cookSnapService;
     RecipeService recipeService;
     ImageService imageService;
     UserService userService;

    @PostMapping("/save/{recipeId}")
    public String saveCookSnap(@PathVariable Long recipeId, @RequestParam("image") MultipartFile image,
                               @RequestParam("content") String content, Model model) {
        try {
            // Lấy recipe từ recipeId
            Recipe recipe = recipeService.getRecipeById(recipeId);
            if (recipe == null) {
                model.addAttribute("error", "Không tìm thấy món ăn!");
                return "redirect:/recipe/" + recipeId;
            }

            // Xử lý upload file ảnh
            String imageUrl = this.imageService.saveImage(image,"CookSnap");


            // Tạo và lưu CookSnap
            CookSnap cookSnap = new CookSnap();
            cookSnap.setContent(content);
            cookSnap.setUrlImage(imageUrl);
            cookSnapService.saveCookSnap(cookSnap, recipe);

            return "redirect:/recipe/" + recipeId;
        } catch (IOException e) {
            model.addAttribute("error", "Lỗi khi upload ảnh!");
            return "redirect:/recipe/" + recipeId;
        }
    }

    @GetMapping("")
    public String getCookedRecipes(Model model) {

        User user = this.userService.getCurrentUser();

        List<CookSnap> cookSnaps = cookSnapService.getCookSnapByUser(user);
        model.addAttribute("cookSnaps", cookSnaps);
        return "client/CookSnap/show"; // Thymeleaf template name
    }
}