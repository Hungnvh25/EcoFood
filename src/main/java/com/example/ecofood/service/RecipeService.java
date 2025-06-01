package com.example.ecofood.service;

import com.example.ecofood.Util.RecipeUtils;
import com.example.ecofood.domain.*;
import com.example.ecofood.repository.IngredientRepository;
import com.example.ecofood.repository.RecipeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
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
    RecipeUtils recipeUtils = new RecipeUtils();

    GeminiService geminiService;
    TextToSpeechService textToSpeechService;


    public List<Recipe> getAllRecipes() {
        List<Recipe> recipeList = recipeRepository.findAll();
        for (Recipe recipe : recipeList) {
            userRecipeLikeService.getUserLikeRecipe(recipe);
        }
        return recipeList;
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


            if (!imageFile.isEmpty()) {
                String imageUrlRecipe = this.imageService.saveImage(imageFile, "Recipe");
                recipe.setImageUrl(imageUrlRecipe);
            }

            User user = this.userService.getCurrentUser();
            recipe.setUser(user);

            HashSet<RecipeIngredient> recipeIngredientHashSet = new HashSet<>();

            // lưu nguyên liệu
            if (ingredientIds != null && !ingredientIds.isEmpty()) {
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
            }

            recipe.setRecipeIngredients(recipeIngredientHashSet);

            // Lưu bước làm

            HashSet<Instruction> instructionHashSet = new HashSet<>();

            if (instructionDescriptions != null && !instructionDescriptions.isEmpty()) {
                for (int i = 0; i < instructionDescriptions.size(); i++) {

                    String imageUrl = this.imageService.saveImage(instructionImages.get(i), "instruction");
                    Instruction instruction = Instruction.builder()
                            .imageUrl(imageUrl)
                            .step((long) (i+1))
                            .description(instructionDescriptions.get(i))
                            .build();
                    instructionHashSet.add(instruction);
                }
            }

            recipe.setInstructions(instructionHashSet);

            // Lưu độ khó, ...
            Category category = Category.builder()
                    .difficulty(Category.Difficulty.valueOf(difficulty))
                    .mealType(Category.MealType.valueOf(mealType))
                    .recipe(recipe)
                    .build();

            recipe.setCategory(category);

            // chuẩn hóa tên món
            String tileName = recipeUtils.normalizeRecipeName(recipe.getTitle());
            recipe.setTileName(tileName);


            // tạo textAudio
            StringBuilder textBuilder = new StringBuilder();

            textBuilder.append("Tên món ăn: ").append(recipe.getTitle()).append(". ");

            if (recipe.getDescription() != null && !recipe.getDescription().isBlank()) {
                textBuilder.append("Thông tin món ăn: ").append(recipe.getDescription()).append(". ");
            }

            if (instructionDescriptions != null && !instructionDescriptions.isEmpty()) {
                for (int i = 0; i < instructionDescriptions.size(); i++) {
                    textBuilder.append("Bước ").append(i + 1).append(": ")
                            .append(instructionDescriptions.get(i)).append(". ");
                }
            }


            String textAudio = textBuilder.toString();
            System.out.println("📢 AudioText = " + textAudio);
            String geminiTextAudio = this.geminiService.generateText(textAudio);
            System.out.println("📢 GeminiTextAudio = " + geminiTextAudio);

            recipe.setTextAudio(geminiTextAudio);


            // tạo audio

            HashSet<Audio> audioHashSet = new HashSet<>();

            String voidCode = String.valueOf(user.getUserSetting().getAccent());

            String urlAudio = this.textToSpeechService.convertTextToSpeech(geminiTextAudio,voidCode ,1.0F);

            Audio audio = Audio.builder()
                    .accent(UserSetting.Accent.fromValue(voidCode))
                    .urlAudio(urlAudio)
                    .voiceGender(user.getUserSetting().getVoiceGender())
                    .recipe(recipe)
                    .build();
            audioHashSet.add(audio);

            recipe.setAudios(audioHashSet);

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

    public Recipe getRecipeById(Long id) {
        Recipe recipe = this.recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with ID: " + id));

        // Sắp xếp theo bước
        Set<Instruction> sortedInstructionsAsSet = recipe.getInstructions()
                .stream()
                .sorted(Comparator.comparing(Instruction::getStep))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        recipe.setInstructions(sortedInstructionsAsSet);

        return recipe;
    }



    public void saveRecipe(Recipe recipe) {
        this.recipeRepository.save(recipe);
    }

    public List<Recipe> findTop3ByOrderByLikeCountDesc() {
        return this.recipeRepository.findTop3ByOrderByLikeCountDesc();
    }

    public List<Recipe> findTop4ByTileNameLike(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return this.recipeRepository.findTop4ByTileNameContainingIgnoreCase(recipeUtils.normalizeRecipeName(keyword));
    }




    public Page<Recipe> getAllRecipes(Pageable pageable) {
        return recipeRepository.findAll(pageable);
    }

    public Page<Recipe> getAllApprovedRecipes(Pageable pageable) {
        return recipeRepository.findByIsPendingRecipeFalse(pageable); // Lấy công thức đã duyệt
    }

    public Page<Recipe> searchRecipes(String title, String userName, Pageable pageable) {
        if (title != null && !title.isEmpty() && userName != null && !userName.isEmpty()) {
            return recipeRepository.findByTitleContainingIgnoreCaseAndUserUserNameContainingIgnoreCase(title, userName, pageable);
        } else if (title != null && !title.isEmpty()) {
            return recipeRepository.findByTitleContainingIgnoreCase(title, pageable);
        } else if (userName != null && !userName.isEmpty()) {
            return recipeRepository.findByUserUserNameContainingIgnoreCase(userName, pageable);
        } else {
            return recipeRepository.findAll(pageable);
        }
    }

    public Page<Recipe> searchApprovedRecipes(String title, String userName, Pageable pageable) {
        if (title != null && !title.isEmpty() && userName != null && !userName.isEmpty()) {
            return recipeRepository.findByTitleContainingIgnoreCaseAndUserUserNameContainingIgnoreCaseAndIsPendingRecipeFalse(title, userName, pageable);
        } else if (title != null && !title.isEmpty()) {
            return recipeRepository.findByTitleContainingIgnoreCaseAndIsPendingRecipeFalse(title, pageable);
        } else if (userName != null && !userName.isEmpty()) {
            return recipeRepository.findByUserUserNameContainingIgnoreCaseAndIsPendingRecipeFalse(userName, pageable);
        } else {
            return recipeRepository.findByIsPendingRecipeFalse(pageable);
        }
    }


    public void deleteRecipe(Long id) {
        if (!recipeRepository.existsById(id)) {
            throw new IllegalArgumentException("Recipe with ID " + id + " does not exist.");
        }
        recipeRepository.deleteById(id);
    }

    public void createRecipe(Recipe recipe) {
        recipe.setCreatedDate(java.time.LocalDate.now());
        recipe.setIsPendingRecipe(true); // Mặc định công thức mới là pending
        recipeRepository.save(recipe);
    }

    public long getTotalRecipes() {
        return recipeRepository.count();
    }

    public long getTotalApprovedRecipes() {
        return recipeRepository.countByIsPendingRecipeFalse(); // Đếm công thức đã duyệt
    }

    public long getPendingRecipesCount() {
        return recipeRepository.countByIsPendingRecipeTrue();
    }

    public long getTotalLikes() {
        return recipeRepository.sumLikeCount();
    }

    public List<Recipe> findByUserId(Long id){
        return this.recipeRepository.findByUserId(id);
    }
}
