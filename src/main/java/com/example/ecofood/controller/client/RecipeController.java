package com.example.ecofood.controller.client;

import com.example.ecofood.domain.DTO.IngredientDTO;
import com.example.ecofood.domain.DTO.RecipeDTO;
import com.example.ecofood.domain.Ingredient;
import com.example.ecofood.domain.Instruction;
import com.example.ecofood.domain.Recipe;
import com.example.ecofood.service.IngredientService;
import com.example.ecofood.service.RecipeService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecipeController {

    RecipeService recipeService;
    IngredientService ingredientService;



    @GetMapping("/recipe")
    public String showCreateForm(Model model) {
        Recipe recipe = new Recipe();
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
}

