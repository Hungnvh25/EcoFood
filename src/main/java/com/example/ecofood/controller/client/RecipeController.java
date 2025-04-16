package com.example.ecofood.controller.client;

import com.example.ecofood.domain.Ingredient;
import com.example.ecofood.domain.Instruction;
import com.example.ecofood.domain.Recipe;
import com.example.ecofood.service.RecipeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Controller
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("/recipe")
    public String showCreateForm(Model model) {
        Recipe recipe = new Recipe();
        recipe.setIngredients(new HashSet<>());
        recipe.setInstructions(new HashSet<>());
        model.addAttribute("recipe", recipe);
        return "client/Recipe/add";
    }

    @PostMapping("/recipes")
    public String saveRecipe(
            @Valid @ModelAttribute Recipe recipe,
            BindingResult result,
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam(value = "name", required = false) List<String> ingredientNames,
            @RequestParam(value = "description", required = false) List<String> ingredientDescriptions,
            @RequestParam(value = "id", required = false) List<Long> ingredientIds,
            @RequestParam(value = "description", required = false) List<String> instructionDescriptions,
            @RequestParam(value = "step", required = false) List<Long> instructionSteps,
            @RequestParam(value = "image", required = false) List<MultipartFile> instructionImages,
            @RequestParam(value = "imageUrl", required = false) List<String> instructionImageUrls) throws IOException {

        if (result.hasErrors()) {
            System.err.println("Validation errors: " + result.getAllErrors());
            return "client/Recipe/add";
        }



        // Save recipe
        recipeService.save(recipe);

        return "client/Recipe/add";
    }
}

