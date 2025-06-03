package com.example.ecofood.service;

import com.example.ecofood.domain.Recipe;
import com.example.ecofood.domain.SaveRecipe;
import com.example.ecofood.repository.SaveRecipeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SaveUserService {
        SaveRecipeRepository saveRecipeRepository;
        RecipeService recipeService;
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


}
