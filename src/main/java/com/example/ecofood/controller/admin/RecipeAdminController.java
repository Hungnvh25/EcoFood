package com.example.ecofood.controller.admin;

import com.example.ecofood.DTO.RecipeDetailDto;
import com.example.ecofood.domain.Recipe;
import com.example.ecofood.service.NotificationService;
import com.example.ecofood.service.RecipeService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecipeAdminController {
    RecipeService recipeService;
    NotificationService notificationService;
    @GetMapping("/recipe")
    public String getRecipeDashboard(Model model,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(required = false) String title,
                                     @RequestParam(required = false) String userName) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Recipe> recipes;
        if ((title != null && !title.isEmpty()) || (userName != null && !userName.isEmpty())) {
            recipes = this.recipeService.searchApprovedRecipes(title, userName, pageable);
        } else {
            recipes = this.recipeService.getAllApprovedRecipes(pageable);
        }

        model.addAttribute("recipes", recipes);
        model.addAttribute("totalRecipes", this.recipeService.getTotalApprovedRecipes());
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
        try {
            this.recipeService.saveRecipe(recipe);
            redirectAttributes.addFlashAttribute("success", "Công thức đã được cập nhật thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể cập nhật công thức: " + e.getMessage());
        }
        return "redirect:/admin/recipe";
    }

    @PostMapping("/recipe/delete")
    public String deleteRecipe(@RequestParam("id") String id, RedirectAttributes redirectAttributes) {
        try {
            if (id == null || id.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Yêu cầu ID công thức.");
                return "redirect:/admin/recipe";
            }
            this.recipeService.deleteRecipe(Long.parseLong(id));
            redirectAttributes.addFlashAttribute("success", "Công thức đã được xóa thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa công thức: " + e.getMessage());
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
            StringBuilder errorMessage = new StringBuilder();
            result.getAllErrors().forEach(error -> errorMessage.append(error.getDefaultMessage()).append("; "));
            redirectAttributes.addFlashAttribute("error", errorMessage.toString());
            return "admin/recipe/addRecipe";
        }

        try {
            this.recipeService.createRecipe(recipe);
            redirectAttributes.addFlashAttribute("success", "Công thức đã được thêm thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể thêm công thức: " + e.getMessage());
        }
        return "redirect:/admin/recipe";
    }

    @GetMapping("/pending-recipes")
    public String getPendingRecipes(Model model,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(required = false) String title,
                                    @RequestParam(required = false) String userName) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Recipe> recipes;

        if ((title != null && !title.isEmpty()) || (userName != null && !userName.isEmpty())) {
            recipes = this.recipeService.searchPendingRecipes(title, userName, pageable);
        } else {
            recipes = this.recipeService.getAllPendingRecipes(pageable);
        }

        model.addAttribute("recipes", recipes);
        model.addAttribute("totalRecipes", this.recipeService.getTotalApprovedRecipes());
        model.addAttribute("pendingRecipes", this.recipeService.getPendingRecipesCount());
        model.addAttribute("totalLikes", this.recipeService.getTotalLikes());

        return "admin/recipe/pendingRecipes";
    }

    @PostMapping("/recipe/approve")
    public String approveRecipe(@RequestParam("id") String id, RedirectAttributes redirectAttributes) {
        try {
            if (id == null || id.isEmpty()) {redirectAttributes.addFlashAttribute("error", "Yêu cầu ID công thức.");
                return "redirect:/admin/pending-recipes";
            }
            Long recipeId = Long.parseLong(id);
            Recipe recipe = this.recipeService.getRecipeById(recipeId);
            if (!recipe.getIsPendingRecipe()) {
                redirectAttributes.addFlashAttribute("error", "Công thức này không ở trạng thái đang chờ duyệt.");
                return "redirect:/admin/pending-recipes";
            }
            this.recipeService.approveRecipe(recipeId);
            this.notificationService.createRecipeStatusNotification(recipe,true);
            redirectAttributes.addFlashAttribute("success", "Công thức được phê duyệt thành công.");
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("error", "ID công thức không hợp lệ.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể phê duyệt công thức: " + e.getMessage());
        }
        return "redirect:/admin/pending-recipes";
    }

    @PostMapping("/recipe/pending/delete")
    public String deletePendingRecipe(@RequestParam("id") String id, RedirectAttributes redirectAttributes) {
        try {
            if (id == null || id.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Yêu cầu ID công thức.");
                return "redirect:/admin/pending-recipes";
            }
            Long recipeId = Long.parseLong(id);
            Recipe recipe = this.recipeService.getRecipeById(recipeId);
            if (!recipe.getIsPendingRecipe()) {
                redirectAttributes.addFlashAttribute("error", "Chỉ có thể xóa công thức đang chờ duyệt.");
                return "redirect:/admin/pending-recipes";
            }
            this.recipeService.deleteRecipe(recipeId);
            redirectAttributes.addFlashAttribute("success", "Công thức đã được xóa thành công.");
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("error", "ID công thức không hợp lệ.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa công thức: " + e.getMessage());
        }
        return "redirect:/admin/pending-recipes";
    }

    @GetMapping("/recipe/similar")
    @ResponseBody
    public ResponseEntity<List<RecipeDetailDto>> getSimilarRecipes(@RequestParam("title") String title) {
        try {
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.ok(List.of());
            }
            List<RecipeDetailDto> similarRecipes = this.recipeService.findTopSimilarByTileNameDTO(title.trim(), 3);
            return ResponseEntity.ok(similarRecipes);
        } catch (Exception e) {
            System.err.println("Lỗi trong getSimilarRecipes: " + e.getMessage());
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @PostMapping("/recipe/setParent")
    public String setParentRecipe(@RequestParam("recipeId") String recipeId,
                                  @RequestParam(value = "parentId", required = false) String parentId,
                                  RedirectAttributes redirectAttributes) {
        try {
            if (recipeId == null || recipeId.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Yêu cầu ID công thức.");
                return "redirect:/admin/pending-recipes";
            }
            Long recipeIdLong = Long.parseLong(recipeId);
            Long parentIdLong = (parentId != null && !parentId.isEmpty()) ? Long.parseLong(parentId) : null;
            if (parentIdLong != null) {
                Recipe parentRecipe = this.recipeService.getRecipeById(parentIdLong);
                if (parentRecipe.getIsPendingRecipe()) {
                    redirectAttributes.addFlashAttribute("error", "Công thức cha không được ở trạng thái đang chờ duyệt.");
                    return "redirect:/admin/pending-recipes";
                }
            }
            this.recipeService.setParentRecipe(recipeIdLong, parentIdLong);
            redirectAttributes.addFlashAttribute("success", "Công thức cha được thiết lập thành công.");
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("error", "ID công thức hoặc ID công thức cha không hợp lệ.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể thiết lập công thức cha: " + e.getMessage());
        }
        return "redirect:/admin/pending-recipes";
    }

    @GetMapping("/recipe/details/{id}")
    @ResponseBody
    public ResponseEntity<RecipeDetailDto> getRecipeDetails(@PathVariable Long id) {
        try {
            Recipe recipe = this.recipeService.getRecipeById(id);
            if (recipe == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(this.recipeService.mapToRecipeDetailDto(recipe));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}