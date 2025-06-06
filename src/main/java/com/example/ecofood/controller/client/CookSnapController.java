package com.example.ecofood.controller.client;

import com.example.ecofood.domain.CookSnap;
import com.example.ecofood.domain.Recipe;
import com.example.ecofood.service.CookSnapService;
import com.example.ecofood.service.ImageService;
import com.example.ecofood.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/cook-snap")
public class CookSnapController {

    @Autowired
    private CookSnapService cookSnapService;

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private ImageService imageService;

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
}