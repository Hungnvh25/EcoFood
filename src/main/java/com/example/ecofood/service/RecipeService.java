package com.example.ecofood.service;

import com.example.ecofood.domain.*;
import com.example.ecofood.DTO.InstructionDTO;
import com.example.ecofood.DTO.RecipeDTO;
import com.example.ecofood.DTO.RecipeIngredientDTO;
import com.example.ecofood.repository.IngredientRepository;
import com.example.ecofood.repository.RecipeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecipeService {

    RecipeRepository recipeRepository;
    IngredientRepository ingredientRepository;
    ImageService imageService;
    UserService userService;
    UserRecipeLikeService userRecipeLikeService;




    public List<Recipe> getAllRecipes() {
        List<Recipe> recipeList = recipeRepository.findAll();
        for (Recipe recipe : recipeList) {
            userRecipeLikeService.getUserLikeRecipe(recipe);
        }
        return recipeList;
    }


    public RecipeDTO convertToDTO(Recipe recipe) {
        return RecipeDTO.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .preparationTime(recipe.getPreparationTime())
                .cookingTime(recipe.getCookingTime())
                .servingSize(recipe.getServingSize())
                .imageUrl(recipe.getImageUrl())
                .user(recipe.getUser())
                .createdDate(recipe.getCreatedDate())
                .updatedDate(recipe.getUpdatedDate())
                .totalCalories(recipe.getTotalCalories())
                .totalProtein(recipe.getTotalProtein())
                .totalFat(recipe.getTotalFat())
                .totalCarbohydrates(recipe.getTotalCarbohydrates())
                .userId(recipe.getUser() != null ? recipe.getUser().getId() : null)
                .instructions(recipe.getInstructions().stream()
                        .map(instruction -> new InstructionDTO(/* map fields */))
                        .collect(Collectors.toSet()))
                .recipeIngredients(recipe.getRecipeIngredients().stream()
                        .map(ingredient -> new RecipeIngredientDTO(/* map fields */))
                        .collect(Collectors.toSet()))
                .commentIds(recipe.getComments().stream().map(Comment::getId).collect(Collectors.toSet()))
                .cookSnapCount(recipe.getCookSnap() != null ? 1 : 0) // Giả định, bạn cần tính thực tế
                .cookSnaps(Collections.emptySet()) // Giả định, bạn cần ánh xạ từ CookSnap
                .build();
    }

    public void createRecipe(Recipe recipe, MultipartFile imageFile,
                             List<Long> ingredientIds,
                             List<Float> ingredientQuantities,
                             List<String> ingredientUnits,
                             List<String> instructionDescriptions,
                             List<MultipartFile> instructionImages,
                             String difficulty,
                             String mealType) {



        try {
            String imageUrlRecipe = this.imageService.saveImage(imageFile, "Recipe");
            recipe.setImageUrl(imageUrlRecipe);
            User user = this.userService.getCurrentUser();

            recipe.setUser(user);

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
            HashSet<Category> categoryHashSet = new HashSet<>();

            // Lưu độ khó, ...
            Category category = Category.builder()
                    .difficulty(Category.Difficulty.valueOf(difficulty))
                    .mealType(Category.MealType.valueOf(mealType))
                    .recipe(recipe)
                    .build();

            recipe.setCategory(category);



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

    public List<Recipe> searchRecipesByTitle(String keyword) {
        return recipeRepository.findByTitleContainingIgnoreCase(keyword);
    }

    public Recipe getRecipeById(Long id){
        return this.recipeRepository.findById(id).orElseThrow(null);
    }

    public void saveRecipe(Recipe recipe){
        this.recipeRepository.save(recipe);
    }

    public List<Recipe> findTop3ByOrderByLikeCountDesc(){
        return this.recipeRepository.findTop3ByOrderByLikeCountDesc();
    }

}
