package com.example.ecofood.service;

import com.example.ecofood.DTO.IngredientDTO;
import com.example.ecofood.DTO.InstructionDTO;
import com.example.ecofood.DTO.RecipeDetailDto;
import com.example.ecofood.DTO.RecipeIngredientDTO;
import com.example.ecofood.Util.RecipeUtils;
import com.example.ecofood.domain.*;
import com.example.ecofood.repository.IngredientRepository;
import com.example.ecofood.repository.RecipeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.text.similarity.LevenshteinDistance;
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

    LevenshteinDistance levenshteinDistance;

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipeList = recipeRepository.findAll();
        for (Recipe recipe : recipeList) {
            userRecipeLikeService.getUserLikeRecipe(recipe);
        }
        return recipeList;
    }


    public List<Recipe> getAllRecipesIsPendingFalse() {
        List<Recipe> recipeList = this.recipeRepository.findRecipeByIsPendingRecipeFalse();
        for (Recipe recipe : recipeList) {
            this.userRecipeLikeService.getUserLikeRecipe(recipe);
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
                             String mealType,
                             String region) {


        try {

            // Sửa tiêu đề
            String formatTile = recipeUtils.normalizeRecipeName(recipe.getTitle());
            recipe.setTitle(formatTile);

            //set imagge
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
                            .step((long) (i + 1))
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
                    .region(Category.Region.valueOf(region))
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

        return this.recipeRepository.findTop5ByTitleContainingIgnoreCase(recipeUtils.normalizeRecipeName(keyword));
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

    public List<Recipe> findByUserId(Long id) {
        return this.recipeRepository.findByUserId(id);
    }

    public List<Recipe> findRecipeByIsPendingRecipeNull(Long id) {
        return this.recipeRepository.findByIsPendingRecipeNullAndUserId(id);
    }

    public Page<Recipe> searchPendingRecipes(String title, String userName, Pageable pageable) {
        if (title != null && !title.isEmpty() && userName != null && !userName.isEmpty()) {
            return recipeRepository.findByTitleContainingIgnoreCaseAndUserUserNameContainingIgnoreCaseAndIsPendingRecipeTrue(title, userName, pageable);
        } else if (title != null && !title.isEmpty()) {
            return recipeRepository.findByTitleContainingIgnoreCaseAndIsPendingRecipeTrue(title, pageable);
        } else if (userName != null && !userName.isEmpty()) {
            return recipeRepository.findByUserUserNameContainingIgnoreCaseAndIsPendingRecipeTrue(userName, pageable);
        } else {
            return getAllPendingRecipes(pageable);
        }
    }

    public Page<Recipe> getAllPendingRecipes(Pageable pageable) {
        return recipeRepository.findByIsPendingRecipeTrue(pageable);
    }

    public void approveRecipe(Long id) throws IOException {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found"));
        recipe.setIsPendingRecipe(false);

        // tạo audio

        User user = recipe.getUser();
        HashSet<Audio> audioHashSet = new HashSet<>();

        String voidCode = String.valueOf(user.getUserSetting().getAccent());
        String geminiTextAudio = recipe.getTextAudio();
        String urlAudio = this.textToSpeechService.convertTextToSpeech(geminiTextAudio, voidCode, 1.0F);

        Audio audio = Audio.builder()
                .accent(UserSetting.Accent.fromValue(voidCode))
                .urlAudio(urlAudio)
                .voiceGender(user.getUserSetting().getVoiceGender())
                .recipe(recipe)
                .build();
        audioHashSet.add(audio);

        recipe.setAudios(audioHashSet);
        recipeRepository.save(recipe);
    }

    public List<Recipe> findTop5ByTitleContainingIgnoreCase(String title) {
        return recipeRepository.findTop5ByTitleContainingIgnoreCase(title);
    }

    public void setParentRecipe(Long recipeId, Long parentId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found"));
        recipe.setParentId(parentId);
        recipeRepository.save(recipe);
    }

    public List<Recipe> findTopSimilarByTileName(String text, int top) {
        List<Recipe> allRecipes = recipeRepository.findAll();

        Map<Recipe, Integer> distanceMap = new HashMap<>();
        for (Recipe recipe : allRecipes) {
            if (recipe.getTileName() != null) {
                int distance = levenshteinDistance.apply(text.toLowerCase(), recipe.getTileName().toLowerCase());
                distanceMap.put(recipe, distance);
            }
        }


        return distanceMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(top)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<RecipeDetailDto> findTopSimilarByTileNameDTO(String text, int top) {
        List<Recipe> similarRecipes = findTopSimilarByTileName(text, top);

        return similarRecipes.stream()
                .map(this::mapToRecipeDetailDto)
                .collect(Collectors.toList());
    }

    public RecipeDetailDto mapToRecipeDetailDto(Recipe recipe) {
        return RecipeDetailDto.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .tileName(recipe.getTileName())
                .imageUrl(recipe.getImageUrl())
                .cookingTime(recipe.getCookingTime())
                .servingSize(recipe.getServingSize())
                .likeCount(recipe.getLikeCount())
                .totalCalories(recipe.getTotalCalories())
                .totalProtein(recipe.getTotalProtein())
                .totalFat(recipe.getTotalFat())
                .totalCarbohydrates(recipe.getTotalCarbohydrates())
                .recipeIngredients(recipe.getRecipeIngredients() != null ? recipe.getRecipeIngredients().stream()
                        .map(ingredient -> RecipeIngredientDTO.builder()
                                .id(ingredient.getId())
                                .ingredient(new IngredientDTO(ingredient.getIngredient().getId(), ingredient.getIngredient().getName()))
                                .quantity(Double.valueOf(ingredient.getQuantity()))
                                .unit(String.valueOf(ingredient.getUnit()))
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .instructions(recipe.getInstructions() != null ? recipe.getInstructions().stream()
                        .map(instruction -> InstructionDTO.builder()
                                .description(instruction.getDescription())
                                .step(Math.toIntExact(instruction.getStep()))
                                .imageUrl(instruction.getImageUrl())
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .build();
    }

    public List<Recipe> searchRecipesByTitleAndFilterMyRecipe(List<Recipe> recipes) {

        User user = this.userService.getCurrentUser();
        List<Recipe> recipesAllUser = this.recipeRepository.findByUserId(user.getId());

        Set<Long> myRecipeIds = new HashSet<>();
        for (Recipe recipe : recipesAllUser) {
            myRecipeIds.add(recipe.getId());
        }

        List<Recipe> recipesFilter = new ArrayList<>(recipes);
        recipesFilter.removeIf(recipe -> !myRecipeIds.contains(recipe.getId()));

        return recipes;
    }


    public List<Recipe> searchRecipesByTitleAndFilterMyRecipeTest(List<Recipe> recipes) {

        User user = this.userService.getCurrentUser();
        List<Recipe> recipesAllUser = this.recipeRepository.findByIsPendingRecipeNullAndUserId(user.getId());

        Set<Long> myRecipeIds = new HashSet<>();
        for (Recipe recipe : recipesAllUser) {
            myRecipeIds.add(recipe.getId());
        }

        List<Recipe> recipesFilter = new ArrayList<>(recipes);
        recipesFilter.removeIf(recipe -> !myRecipeIds.contains(recipe.getId()));

        return recipes;
    }


    public List<Recipe> searchRecipesByTitleAndFilters(String keyword, Category.Difficulty difficulty,
                                                       Category.MealType mealType, Category.Region region) {
        return recipeRepository.findByTitleContainingIgnoreCaseAndCategoryDifficultyAndCategoryMealTypeAndCategoryRegion(
                keyword, difficulty, mealType, region);

    }


    public List<RecipeDetailDto> getRelatedRecipeDetails(Long parentId) {
        List<Recipe> relatedRecipes = recipeRepository.findByParentId(parentId);
        return relatedRecipes.stream()
                .map(this::mapToRecipeDetailDto)
                .collect(Collectors.toList());
    }

    public List<Recipe> remoteRecipeListIsPremiumNull(List<Recipe> recipes) {
        recipes.removeIf(recipe -> recipe.getIsPendingRecipe() == null);
        return recipes;
    }


}
