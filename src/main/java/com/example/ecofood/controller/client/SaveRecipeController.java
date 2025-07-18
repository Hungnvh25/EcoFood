
package com.example.ecofood.controller.client;

import com.example.ecofood.domain.Category;
import com.example.ecofood.domain.Recipe;
import com.example.ecofood.domain.SaveRecipe;
import com.example.ecofood.domain.User;
import com.example.ecofood.repository.SaveRecipeRepository;
import com.example.ecofood.service.RecipeService;
import com.example.ecofood.service.SaveUserService;
import com.example.ecofood.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SaveRecipeController {
    RecipeService recipeService;
    UserService userService;
    SaveRecipeRepository saveRecipeRepository;
    SaveUserService saveUserService;

    @PostMapping("/recipe/{id}/save")
    public ResponseEntity<Map<String, Boolean>> toggleSaveRecipe(@PathVariable Long id) {
        User user = this.userService.getCurrentUser();
        Recipe recipe = this.recipeService.getRecipeById(id);
        Optional<SaveRecipe> existingSave = saveRecipeRepository.findByUserAndRecipe(user, recipe);

        boolean saved;
        if (existingSave.isPresent()) {
            // Bỏ lưu
            saveRecipeRepository.delete(existingSave.get());
            saved = false;
        } else {
            // Lưu món
            SaveRecipe saveRecipe = SaveRecipe.builder()
                    .user(user)
                    .recipe(recipe)
                    .build();
            saveRecipeRepository.save(saveRecipe);
            saved = true;
        }

        return ResponseEntity.ok(Collections.singletonMap("saved", saved));
    }

    @GetMapping("/recipe/{id}/save")
    public ResponseEntity<Map<String, Boolean>> getSaveStatus(@PathVariable Long id) {
        User user = userService.getCurrentUser();
        Recipe recipe = recipeService.getRecipeById(id);
        boolean isSaved = saveRecipeRepository.findByUserAndRecipe(user, recipe).isPresent();
        return ResponseEntity.ok(Collections.singletonMap("saved", isSaved));
    }

    @GetMapping("/saveRecipe")
    public String getPageSaveRecipe(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "difficulty", required = false) Category.Difficulty difficulty,
            @RequestParam(name = "mealType", required = false) Category.MealType mealType,
            @RequestParam(name = "region", required = false) Category.Region region,
            Model model) {

        List<Recipe> recipes;
        User user = this.userService.getCurrentUser();

        if((keyword != null && !keyword.isEmpty()) ||
                (difficulty != null && !difficulty.name().isEmpty()) ||
                (mealType != null && !mealType.name().isEmpty()) ||
                (region != null && !region.name().isEmpty())){
            recipes = this.recipeService.searchRecipesByTitleAndFilters(keyword, difficulty, mealType, region);
            recipes = this.saveUserService.searchRecipesByTitleAndFiltersSaveRecipe(recipes);
        }
        else {
            recipes = this.saveUserService.getAllSaveRecipeUser(user.getId());
        }

        recipes = this.recipeService.remoteRecipeListIsPremiumNull(recipes);
        model.addAttribute("recipes", recipes);
        model.addAttribute("keyword", keyword);
        model.addAttribute("difficulty", difficulty);
        model.addAttribute("mealType", mealType);
        model.addAttribute("region", region);

        // Truyền danh sách enum để hiển thị filter
        model.addAttribute("difficulties", Category.Difficulty.values());
        model.addAttribute("mealTypes", Category.MealType.values());
        model.addAttribute("regions", Category.Region.values());

        return "client/SaveRecipe/show";
    }

}
