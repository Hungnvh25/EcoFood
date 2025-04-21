package com.example.ecofood.service;

import com.example.ecofood.domain.DTO.RecipeDTO;
import com.example.ecofood.domain.Ingredient;
import com.example.ecofood.domain.Instruction;
import com.example.ecofood.domain.Recipe;
import com.example.ecofood.domain.RecipeIngredient;
import com.example.ecofood.repository.IngredientRepository;
import com.example.ecofood.repository.RecipeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecipeService {

    RecipeRepository recipeRepository;
    IngredientRepository ingredientRepository;
    ImageService imageService;

    public void createRecipe(Recipe recipe, MultipartFile imageFile,
                             List<Long> ingredientIds,
                             List<Float> ingredientQuantities,
                             List<String> ingredientUnits,
                             List<String> instructionDescriptions,
                             List<MultipartFile> instructionImages) {



        try {
            String imageUrlRecipe = this.imageService.saveImage(imageFile, "Recipe");
            recipe.setImageUrl(imageUrlRecipe);
            HashSet<RecipeIngredient> recipeIngredientHashSet = new HashSet<>();
            // lưu nguyên liệu
            for (int i = 0; i < ingredientIds.size(); i++) {

                Ingredient ingredient = this.ingredientRepository.findAllById(ingredientIds.get(i));

                RecipeIngredient recipeIngredient = RecipeIngredient.builder()
                        .ingredient(ingredient)
                        .recipe(recipe)
                        .quantity(ingredientQuantities.get(i))
                        .unit(RecipeIngredient.Unit.valueOf(ingredientUnits.get(i)))
                        .build();
                recipeIngredientHashSet.add(recipeIngredient);

                saveNutrition(recipe, ingredient, recipeIngredient);


            }
            recipe.setRecipeIngredients(recipeIngredientHashSet);

            // Lưu bước làm

            HashSet<Instruction> instructionHashSet = new HashSet<>();
            for (int i = 0; i < instructionDescriptions.size(); i++) {

                String imageUrl = this.imageService.saveImage(instructionImages.get(i), "instruction");
                Instruction instruction = Instruction.builder()
                        .imageUrl(imageUrl)
                        .description(instructionDescriptions.get(i))
                        .build();
                instructionHashSet.add(instruction);
            }
            recipe.setInstructions(instructionHashSet);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        this.recipeRepository.save(recipe);
    }


    private void saveNutrition(Recipe recipe, Ingredient ingredient, RecipeIngredient recipeIngredient) {

        Float totalCalories = recipe.getTotalCalories();
        Float totalProtein = recipe.getTotalProtein();
        Float totalFat = recipe.getTotalFat();
        Float totalCarbohydrates = recipe.getTotalCarbohydrates();

        float gramEquivalent = 0f;

        switch (recipeIngredient.getUnit()) {
            case GRAM:
            case ML:
                gramEquivalent = recipeIngredient.getQuantity();
                break;
            case PIECE:
                gramEquivalent = recipeIngredient.getQuantity() * ingredient.getAvgG();
                break;
            case TABLESPOON:
                gramEquivalent = recipeIngredient.getQuantity() * 14.175f;
                break;
            case TEASPOON:
                gramEquivalent = recipeIngredient.getQuantity() * 5.69f;
                break;
        }

        totalCalories += gramEquivalent / 100 * ingredient.getCaloriesPer100g();
        totalProtein += gramEquivalent / 100 * ingredient.getProteinPer100g();
        totalFat += gramEquivalent / 100 * ingredient.getFatPer100g();
        totalCarbohydrates += gramEquivalent / 100 * ingredient.getCarbohydratesPer100g();

        recipe.setTotalCalories(totalCalories);
        recipe.setTotalProtein(totalProtein);
        recipe.setTotalFat(totalFat);
        recipe.setTotalCarbohydrates(totalCarbohydrates);

    }

}
