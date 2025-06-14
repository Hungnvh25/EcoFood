package com.example.ecofood.controller.client;

import com.example.ecofood.DTO.RecipeDetailDto;
import com.example.ecofood.domain.Category;

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
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/cookSnap")
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
    public String getCookedRecipes(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "difficulty", required = false) Category.Difficulty difficulty,
            @RequestParam(name = "mealType", required = false) Category.MealType mealType,
            @RequestParam(name = "region", required = false) Category.Region region,
            Model model) {

        List<CookSnap> cookSnaps;

        User user = this.userService.getCurrentUser();
        if ((keyword != null && !keyword.isEmpty()) ||
                (difficulty != null && !difficulty.name().isEmpty()) ||
                (mealType != null && !mealType.name().isEmpty()) ||
                (region != null && !region.name().isEmpty())) {

            // Tìm kiếm công thức theo các bộ lọc
            List<Recipe> recipes = this.recipeService.searchRecipesByTitleAndFilters(keyword, difficulty, mealType, region);
            recipes = this.recipeService.remoteRecipeListIsPremiumNull(recipes);
            cookSnaps = this.cookSnapService.getCookSnapByUserFilters(recipes);
        } else {
            // Nếu không có bộ lọc nào, trả về danh sách công thức đầy đủ
            cookSnaps = this.cookSnapService.getCookSnapByUser(user);
        }

        model.addAttribute("cookSnaps", cookSnaps);
        model.addAttribute("keyword", keyword);
        model.addAttribute("difficulty", difficulty);
        model.addAttribute("mealType", mealType);
        model.addAttribute("region", region);

        // Truyền danh sách enum để hiển thị filter
        model.addAttribute("difficulties", Category.Difficulty.values());
        model.addAttribute("mealTypes", Category.MealType.values());
        model.addAttribute("regions", Category.Region.values());

        return "client/CookSnap/show";
    }

    @GetMapping("/{parentId}")
    public ResponseEntity<List<RecipeDetailDto>> getRelatedRecipes(@PathVariable Long parentId) {
        List<RecipeDetailDto> relatedRecipes = this.recipeService.getRelatedRecipeDetails(parentId);
        return ResponseEntity.ok(relatedRecipes);
    }
}