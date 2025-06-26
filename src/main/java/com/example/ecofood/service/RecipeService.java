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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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
    UserActivityService userActivityService;
    RecipeUtils recipeUtils = new RecipeUtils();

    GeminiService geminiService;
    TextToSpeechService textToSpeechService;

    LevenshteinDistance levenshteinDistance;
    CategoryService categoryService;

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
                             List<String> instructionImageUrls,
                             String difficulty,
                             String mealType,
                             String region) {

        try {
            if (recipe.getId() != null){
                recipe = this.getRecipeById(recipe.getId());
                recipe.setIsPendingRecipe(true);
            }

            // Sửa tiêu đề
            String formatTile = recipeUtils.capitalizeFirstLetter(recipe.getTitle());
            recipe.setTitle(formatTile);

            // Cập nhật hình ảnh chính của món ăn
            if (!imageFile.isEmpty()) {
                String imageUrlRecipe = this.imageService.saveImage(imageFile, "Recipe");
                recipe.setImageUrl(imageUrlRecipe);
            }

            User user = this.userService.getCurrentUser();
            recipe.setUser(user);

            // Xử lý nguyên liệu
            if (recipe.getRecipeIngredients() != null) {
                recipe.getRecipeIngredients().clear();
            } else {
                recipe.setRecipeIngredients(new HashSet<>());
            }

            if (ingredientIds != null && !ingredientIds.isEmpty()) {
                for (int i = 0; i < ingredientIds.size(); i++) {
                    Ingredient ingredient = this.ingredientRepository.findAllById(ingredientIds.get(i));

                    RecipeIngredient recipeIngredient = RecipeIngredient.builder()
                            .ingredient(ingredient)
                            .recipe(recipe)
                            .quantity(ingredientQuantities.get(i))
                            .unit(RecipeIngredient.Unit.valueOf(ingredientUnits.get(i)))
                            .build();

                    recipe.getRecipeIngredients().add(recipeIngredient);
                    saveNutrition(recipe, ingredient, recipeIngredient);
                }
            }

            // Xử lý các bước làm món
            if (recipe.getInstructions() != null) {
                recipe.getInstructions().clear();
            } else {
                recipe.setInstructions(new HashSet<>());
            }

            if (instructionDescriptions != null && !instructionDescriptions.isEmpty()) {
                for (int i = 0; i < instructionDescriptions.size(); i++) {
                    String imageUrl = null;

                    // Nếu có file ảnh mới → upload
                    if (instructionImages != null && i < instructionImages.size()
                            && !instructionImages.get(i).isEmpty()) {
                        imageUrl = this.imageService.saveImage(instructionImages.get(i), "instruction");
                    }
                    // Nếu không có file mới nhưng có URL cũ → giữ lại
                    else if (instructionImageUrls != null && i < instructionImageUrls.size()) {
                        imageUrl = instructionImageUrls.get(i);
                    }

                    Instruction instruction = Instruction.builder()
                            .imageUrl(imageUrl)
                            .step((long) (i + 1))
                            .description(instructionDescriptions.get(i))
                            .build();

                    recipe.getInstructions().add(instruction);
                }
            }

            // Xử lý danh mục (category)
            if (recipe.getCategory() != null) {
                Category category = recipe.getCategory();
                category.setRegion(Category.Region.valueOf(region));
                category.setDifficulty(Category.Difficulty.valueOf(difficulty));
                category.setMealType(Category.MealType.valueOf(mealType));
                this.categoryService.save(category);
            } else {
                Category category = Category.builder()
                        .difficulty(Category.Difficulty.valueOf(difficulty))
                        .mealType(Category.MealType.valueOf(mealType))
                        .region(Category.Region.valueOf(region))
                        .recipe(recipe)
                        .build();
                recipe.setCategory(category);
            }

            // Tạo textAudio từ mô tả và các bước
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

            CompletableFuture<String> geminiTextAudioFuture = geminiService.generateTextAsync(textAudio);
            String geminiTextAudio = geminiTextAudioFuture.get();
            recipe.setTextAudio(geminiTextAudio);

            // Chuẩn hóa tên món
            String tileName = recipeUtils.normalizeRecipeName(recipe.getTitle());
            recipe.setTileName(tileName);

            // Lưu recipe cuối cùng
            this.recipeRepository.save(recipe);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void createRecipeIsPendingNull(Recipe recipe, MultipartFile imageFile,
                                          List<Long> ingredientIds,
                                          List<Float> ingredientQuantities,
                                          List<String> ingredientUnits,
                                          List<String> instructionDescriptions,
                                          List<MultipartFile> instructionImages,
                                          List<String> instructionImageUrls,
                                          String difficulty,
                                          String mealType,
                                          String region) {

        try {
            if (recipe.getId() != null){
                recipe = this.getRecipeById(recipe.getId());
            }


            // Sửa tiêu đề nếu có
            if (recipe.getTitle() != null && !recipe.getTitle().isBlank()) {
                String formatTile = recipeUtils.capitalizeFirstLetter(recipe.getTitle());
                recipe.setTitle(formatTile);
                recipe.setTileName(formatTile);
            }

            // Set image nếu có
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrlRecipe = this.imageService.saveImage(imageFile, "Recipe");
                recipe.setImageUrl(imageUrlRecipe);
            }

            User user = this.userService.getCurrentUser();
            recipe.setUser(user);

            // Lưu nguyên liệu nếu các danh sách không null và hợp lệ

            Set<RecipeIngredient> recipeIngredientHashSet = new HashSet<>();

            if (ingredientIds != null && ingredientQuantities != null && ingredientUnits != null
                    && ingredientIds.size() == ingredientQuantities.size() && ingredientQuantities.size() == ingredientUnits.size()) {
                for (int i = 0; i < ingredientIds.size(); i++) {
                    if (ingredientIds.get(i) != null && ingredientQuantities.get(i) != null && ingredientUnits.get(i) != null) {
                        Ingredient ingredient = this.ingredientRepository.findAllById(ingredientIds.get(i));
                        if (ingredient != null) {
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
                }
            }
            recipe.getRecipeIngredients().clear();
            recipe.getRecipeIngredients().addAll(recipeIngredientHashSet);

            // Lưu bước làm nếu có
            if (recipe.getInstructions().isEmpty()){
                recipe.getInstructions().clear();
            }

            HashSet<Instruction> instructionHashSet = new HashSet<>();

            if (instructionDescriptions != null) {
                for (int i = 0; i < instructionDescriptions.size(); i++) {
                    if (instructionDescriptions.get(i) != null && !instructionDescriptions.get(i).isBlank()) {
                        String imageUrl = null;

                        if (instructionImages != null && i < instructionImages.size()
                                && instructionImages.get(i) != null && !instructionImages.get(i).isEmpty()) {
                            imageUrl = this.imageService.saveImage(instructionImages.get(i), "instruction");
                        }
                        else if (instructionImageUrls != null && i < instructionImageUrls.size()) {
                            imageUrl = instructionImageUrls.get(i);
                        }

                        Instruction instruction = Instruction.builder()
                                .imageUrl(imageUrl)
                                .step((long) (i + 1))
                                .description(instructionDescriptions.get(i))
                                .build();
                        instructionHashSet.add(instruction);
                    }
                }
            }
            recipe.setInstructions(instructionHashSet);

            // Lưu danh mục nếu các trường không null
            if(recipe.getCategory() != null){
                recipe.getCategory().setRecipe(recipe);
                recipe.getCategory().setDifficulty(Category.Difficulty.valueOf(difficulty));
                recipe.getCategory().setMealType(Category.MealType.valueOf(mealType));
                recipe.getCategory().setRegion(Category.Region.valueOf(region));
            }else {
                if (!difficulty.isEmpty() && !mealType.isEmpty() && !region.isEmpty()) {
                    Category category = Category.builder()
                            .difficulty(Category.Difficulty.valueOf(difficulty))
                            .mealType(Category.MealType.valueOf(mealType))
                            .region(Category.Region.valueOf(region))
                            .recipe(recipe)
                            .build();
                    recipe.setCategory(category);
                }

            }


            // Tạo textAudio nếu có dữ liệu
            StringBuilder textBuilder = new StringBuilder();
            if (recipe.getTitle() != null && !recipe.getTitle().isBlank()) {
                textBuilder.append("Tên món ăn: ").append(recipe.getTitle()).append(". ");
            }

            if (recipe.getDescription() != null && !recipe.getDescription().isBlank()) {
                textBuilder.append("Thông tin món ăn: ").append(recipe.getDescription()).append(". ");
            }

            if (instructionDescriptions != null && !instructionDescriptions.isEmpty()) {
                for (int i = 0; i < instructionDescriptions.size(); i++) {
                    if (instructionDescriptions.get(i) != null && !instructionDescriptions.get(i).isBlank()) {
                        textBuilder.append("Bước ").append(i + 1).append(": ")
                                .append(instructionDescriptions.get(i)).append(". ");
                    }
                }
            }

            String textAudio = textBuilder.toString();
            System.out.println("📢 AudioText = " + textAudio);

            CompletableFuture<String> geminiTextAudioFuture = geminiService.generateTextAsync(textAudio);

            String geminiTextAudio = geminiTextAudioFuture.get();
            recipe.setTextAudio(geminiTextAudio);
            System.out.println("📢 GEMINI TEXT = " + geminiTextAudio);

            recipe.setTextAudio(geminiTextAudio);

            // Lưu công thức
            this.recipeRepository.save(recipe);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        return this.recipeRepository.findTop3ByIsPendingRecipeFalseOrderByLikeCountDesc();
    }

    public List<Recipe> findTop4ByTileNameLike(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return this.recipeRepository.findTop5ByTitleContainingIgnoreCase(recipeUtils.normalizeRecipeName(keyword));
    }


    public Page<Recipe> getAllApprovedRecipes(Pageable pageable) {
        return recipeRepository.findByIsPendingRecipeFalse(pageable); // Lấy công thức đã duyệt
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

    @Transactional
    public void deleteRecipe(Long id) {
        if (!recipeRepository.existsById(id)) {
            throw new IllegalArgumentException("Recipe with ID " + id + " does not exist.");
        }

        this.userRecipeLikeService.deleteByRecipeId(id);
        Recipe recipe = getRecipeById(id);
        List<UserActivity> userActivities = this.userActivityService.findAllByRecipeIds(id);
        for (UserActivity userActivity : userActivities) {
            userActivity.getRecipeIds().removeIf(recipeId -> recipeId.equals(id));
            this.userActivityService.saveUserActivity(userActivity);
        }

        System.out.println("Deleted recipe "+ recipe.getTitle() + ": ID "+ recipe.getId());
        recipeRepository.deleteById(id);
    }

    public void createRecipe(Recipe recipe) {
        recipe.setCreatedDate(java.time.LocalDate.now());
        recipeRepository.save(recipe);
    }


    public long getTotalApprovedRecipes() {
        return recipeRepository.countByIsPendingRecipeFalse();
    }

    public long getPendingRecipesCount() {
        return recipeRepository.countByIsPendingRecipeTrue();
    }

    public long getTotalLikes() {

        Long sumLike = recipeRepository.sumLikeCount();
        return sumLike != null ? sumLike : 0L;
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

        User user = recipe.getUser();
        String voidCode = String.valueOf(user.getUserSetting().getAccent());

        // Kiểm tra nếu recipe chưa có audios hoặc rỗng → tạo mới
        if (recipe.getAudios() == null || recipe.getAudios().isEmpty()) {
            String geminiTextAudio = recipe.getTextAudio();
            String urlAudio = this.textToSpeechService.convertTextToSpeech(geminiTextAudio, voidCode, 1.0F);

            Audio audio = Audio.builder()
                    .accent(UserSetting.Accent.fromValue(voidCode))
                    .urlAudio(urlAudio)
                    .voiceGender(user.getUserSetting().getVoiceGender())
                    .recipe(recipe)
                    .build();

            recipe.getAudios().clear(); // Đảm bảo không bị duplicate
            recipe.getAudios().add(audio);
        } else {
            // Nếu đã có audio → giữ nguyên, không làm gì thêm
            // Có thể log lại hoặc cập nhật thông tin nếu cần
        }

        recipeRepository.save(recipe);
    }


    public void setParentRecipe(Long recipeId, Long parentId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found"));
        recipe.setParentId(parentId);
        recipeRepository.save(recipe);
    }

    public List<Recipe> findTopSimilarByTileName(String text, int top) {
        List<Recipe> allRecipes = recipeRepository.findByIsPendingRecipeFalse();

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
                .description(recipe.getDescription())
                .preparationTime(recipe.getPreparationTime())
                .cookingTime(recipe.getCookingTime())
                .servingSize(recipe.getServingSize())
                .imageUrl(recipe.getImageUrl())
                .likeCount(recipe.getLikeCount())
                .createdDate(recipe.getCreatedDate())
                .updatedDate(recipe.getUpdatedDate())
                .isPendingRecipe(recipe.getIsPendingRecipe())
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
                .region(recipe.getCategory() != null ? recipe.getCategory().getName() : null) // Thêm thông tin vùng miền từ category (nếu có)
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

    public List<Recipe> remoteRecipeIsPendingRecipeNull(List<Recipe> recipes) {
        return recipes.stream()
                .filter(recipe -> recipe.getIsPendingRecipe() != null && !recipe.getIsPendingRecipe())
                .toList();
    }
}
