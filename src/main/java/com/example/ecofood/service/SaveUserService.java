package com.example.ecofood.service;

import com.example.ecofood.domain.Recipe;
import com.example.ecofood.domain.SaveRecipe;
import com.example.ecofood.domain.User;
import com.example.ecofood.repository.SaveRecipeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SaveUserService {
        SaveRecipeRepository saveRecipeRepository;
        RecipeService recipeService;
        UserService userService;
    public List<SaveRecipe> findSaveRecipeByUserIdAndRecipeId(Long userId){
        return this.saveRecipeRepository.findByUserId(userId);
    }


    public List<Recipe> getAllSaveRecipeUser(Long id){

        List<SaveRecipe> saveRecipes = this.saveRecipeRepository.findByUserId(id);

        List<Recipe> recipes = new ArrayList<>();

        for (SaveRecipe saveRecipe: saveRecipes
             ) {
            Recipe recipe = this.recipeService.getRecipeById(saveRecipe.getRecipe().getId());
            recipes.add(recipe);
        }

        return recipes;
    }

    public List<Recipe> searchRecipesByTitleAndFiltersSaveRecipe(List<Recipe> recipes) {
        User user = this.userService.getCurrentUser();

        List<SaveRecipe> saveRecipes = this.saveRecipeRepository.findByUserId(user.getId());

        Set<Long> savedRecipeIds = new HashSet<>();
        for (SaveRecipe saveRecipe : saveRecipes) {
            savedRecipeIds.add(saveRecipe.getRecipe().getId());
        }

        List<Recipe> recipesFilter = new ArrayList<>(recipes);

        recipesFilter.removeIf(recipe -> !savedRecipeIds.contains(recipe.getId()));

        return recipesFilter;
    }



}
