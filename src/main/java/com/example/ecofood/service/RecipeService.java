package com.example.ecofood.service;

import com.example.ecofood.domain.Ingredient;
import com.example.ecofood.domain.Recipe;
import com.example.ecofood.repository.IngredientRepository;
import com.example.ecofood.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;

    public RecipeService(RecipeRepository recipeRepository, IngredientRepository ingredientRepository) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
    }

    @Transactional
        public void save(Recipe recipe) {
        // Handle ingredients
        Set<Ingredient> managedIngredients = new HashSet<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            // Check if ingredient exists by name
            Ingredient existing = ingredientRepository.findAllByName(ingredient.getName());
            if (!(existing == null)) {
                ingredientRepository.save(ingredient);
            }
            managedIngredients.add(existing);
        }
        recipe.setIngredients(managedIngredients);

        // Save recipe
         recipeRepository.save(recipe);
    }
}
