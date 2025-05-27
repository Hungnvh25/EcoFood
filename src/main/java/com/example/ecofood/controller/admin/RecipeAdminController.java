package com.example.ecofood.controller.admin;



import com.example.ecofood.domain.Recipe;
import com.example.ecofood.service.RecipeService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecipeAdminController {
    RecipeService recipeService;

    @GetMapping("/recipe")
    public String getRecipeDashboard(Model model,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(required = false) String title,
                                     @RequestParam(required = false) String userName) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Recipe> recipes;

        if (title != null && !title.isEmpty() || userName != null && !userName.isEmpty()) {
            recipes = this.recipeService.searchApprovedRecipes(title, userName, pageable); // Chỉ lấy công thức đã duyệt
        } else {
            recipes = this.recipeService.getAllApprovedRecipes(pageable); // Chỉ lấy công thức đã duyệt
        }

        model.addAttribute("recipes", recipes);
        model.addAttribute("totalRecipes", this.recipeService.getTotalApprovedRecipes()); // Cập nhật tổng số công thức đã duyệt
        model.addAttribute("pendingRecipes", this.recipeService.getPendingRecipesCount());
        model.addAttribute("totalLikes", this.recipeService.getTotalLikes());

        if (!model.containsAttribute("editRecipe")) {
            model.addAttribute("editRecipe", new Recipe());
        }
        return "admin/recipe/show";
    }

    @PostMapping("/recipe/update")
    public String updateRecipe(@Valid @ModelAttribute("editRecipe") Recipe recipe, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            result.getAllErrors().forEach(error -> errorMessage.append(error.getDefaultMessage()).append("; "));
            redirectAttributes.addFlashAttribute("error", errorMessage.toString());
            return "redirect:/admin/recipe";
        }

        this.recipeService.saveRecipe(recipe);
        redirectAttributes.addFlashAttribute("success", "Recipe updated successfully.");
        return "redirect:/admin/recipe";
    }

    @PostMapping("/recipe/delete")
    public String deleteRecipe(@RequestParam("id") String id, RedirectAttributes redirectAttributes) {
        try {
            if (id == null) {
                redirectAttributes.addFlashAttribute("error", "Recipe ID is required.");
                return "redirect:/admin/recipe";
            }
            this.recipeService.deleteRecipe(Long.parseLong(id));
            redirectAttributes.addFlashAttribute("success", "Recipe deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete recipe: " + e.getMessage());
        }
        return "redirect:/admin/recipe";
    }

    @GetMapping("/recipe/create")
    public String showAddRecipeForm(Model model) {
        model.addAttribute("newRecipe", new Recipe());
        return "admin/recipe/addRecipe";
    }

    @PostMapping("/recipe/add")
    public String addRecipe(@Valid @ModelAttribute("newRecipe") Recipe recipe, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/recipe/addRecipe";
        }

        this.recipeService.createRecipe(recipe);
        redirectAttributes.addFlashAttribute("success", "Recipe added successfully.");
        return "redirect:/admin/recipe";
    }
}
