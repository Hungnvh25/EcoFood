package com.example.ecofood.controller.client;

import com.example.ecofood.DTO.IngredientDTO;
import com.example.ecofood.DTO.RecipeDTO;
import com.example.ecofood.domain.Ingredient;
import com.example.ecofood.domain.Recipe;
import com.example.ecofood.domain.User;
import com.example.ecofood.service.IngredientService;
import com.example.ecofood.service.RecipeService;
import com.example.ecofood.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecipeController {

    RecipeService recipeService;
    IngredientService ingredientService;
    UserService userService;



    @GetMapping ("/")
    public String getEcoFood(Model model, HttpServletRequest request){


        // Lấy danh sách recipes từ service
        List<RecipeDTO> recipes = this.recipeService.getAllRecipes();
        model.addAttribute("recipes", recipes);

        return "index";
    }
    @GetMapping("/search")
    public String searchRecipes(@RequestParam("keyword") String keyword, Model model) {
        List<RecipeDTO> searchResults = this.recipeService.searchRecipesByTitle(keyword);
        model.addAttribute("searchResults", searchResults);
        model.addAttribute("keyword", keyword);
        return "client/Search/search";
    }

    @GetMapping("/recipe")
    public String showCreateForm(Model model) {
        Recipe recipe = new Recipe();
        User currentUser = this.userService.getCurrentUser();

        model.addAttribute("currentUser",currentUser);
        model.addAttribute("recipe", recipe);
        return "client/Recipe/add";
    }

    @GetMapping("/api/ingredients/search")
    @ResponseBody
    public List<IngredientDTO> searchIngredients(@RequestParam("keyword") String keyword) {
        List<Ingredient> ingredients = this.ingredientService.findAllByNameContaining(keyword);

        return ingredients.stream()
                .map(ingredient -> IngredientDTO.builder()
                        .id(ingredient.getId())
                        .name(ingredient.getName())
                        .build())
                .collect(Collectors.toList());
    }

    @PostMapping("/recipes")
    public String saveRecipe(
            @Valid @ModelAttribute Recipe recipe,
            BindingResult result,
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam(value = "ingredientId", required = false) List<Long> ingredientIds,
            @RequestParam(value = "ingredientQuantities", required = false) List<Float> ingredientQuantities,
            @RequestParam(value = "ingredientUnits", required = false) List<String> ingredientUnits,
            @RequestParam(value = "instructionDescriptions", required = false) List<String> instructionDescriptions,
            @RequestParam(value = "image", required = false) List<MultipartFile> instructionImages) {

        if (result.hasErrors()) {
            return "client/Recipe/add";
        }

        this.recipeService.createRecipe(recipe,imageFile,ingredientIds,ingredientQuantities,ingredientUnits,instructionDescriptions,instructionImages);

        return "redirect:/recipe";
    }

    @GetMapping("/recipe/{id}")
    public String getRecipeDetail(@PathVariable Long id, Model model) {
        try {
            Recipe recipe = this.recipeService.getRecipeById(id);
            model.addAttribute("recipe", recipe);
            return "client/Recipe/recipe-detail";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", "Không tìm thấy món ăn với ID: " + id);
            return "error"; // Giả định bạn có template error.html
        }
    }
}

