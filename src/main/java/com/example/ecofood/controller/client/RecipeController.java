package com.example.ecofood.controller.client;

import com.example.ecofood.DTO.*;
import com.example.ecofood.domain.Ingredient;
import com.example.ecofood.domain.Recipe;
import com.example.ecofood.domain.User;
import com.example.ecofood.domain.UserRecipeLike;
import com.example.ecofood.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecipeController {

    RecipeService recipeService;
    IngredientService ingredientService;
    UserService userService;
    UserActivityService userActivityService;
    UserRecipeLikeService userRecipeLikeService;



    @GetMapping("/")
    public String getEcoFood(Model model, HttpServletRequest request) {
        // Lấy danh sách recipes từ service
        List<Recipe> recipes = this.recipeService.getAllRecipes();
        model.addAttribute("recipes", recipes);

        return "index";
    }

    @GetMapping("/search")
    public String searchRecipes(@RequestParam("keyword") String keyword, Model model) {
        List<Recipe> searchResults = this.recipeService.searchRecipesByTitle(keyword);
        model.addAttribute("searchResults", searchResults);
        model.addAttribute("keyword", keyword);
        return "client/Search/search";
    }

    @GetMapping("/recipe")
    public String showCreateForm(Model model, HttpServletRequest request) {
        Recipe recipe = new Recipe();
        User currentUser = this.userService.getCurrentUser();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("recipe", recipe);
        model.addAttribute("currentUri", request.getRequestURI());
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
            @RequestParam(value = "image", required = false) List<MultipartFile> instructionImages,
            @RequestParam(value = "difficulty", required = true) String difficulty,
            @RequestParam(value = "mealType", required = true) String mealType,
            @RequestParam(value = "publish", defaultValue = "false") boolean publish) {

        if (result.hasErrors()) {
            return "client/Recipe/add";
        }

        if (ingredientIds == null || instructionDescriptions == null) {

        }

        this.recipeService.createRecipe(recipe, imageFile, ingredientIds, ingredientQuantities, ingredientUnits, instructionDescriptions, instructionImages, difficulty, mealType);

        return "redirect:/";
    }

    @GetMapping("/recipe/{id}")
    public String getRecipeDetail(@PathVariable Long id, Model model) {
        try {
            Recipe recipe = this.recipeService.getRecipeById(id);
            model.addAttribute("recipe", recipe);

            this.userActivityService.saveHistoryViewRecipe(recipe);
            return "client/Recipe/recipe-detail";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", "Không tìm thấy món ăn với ID: " + id);
            return "error"; // Giả định bạn có template error.html
        }
    }


    @PostMapping("/recipe/{id}/toggle-like")
    public ResponseEntity<?> toggleLikeRecipe(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();
        Recipe recipe = recipeService.getRecipeById(id);

        // Kiểm tra xem user đã like chưa
        boolean alreadyLiked = this.userRecipeLikeService.existsByUserAndRecipe(recipe, currentUser);

        if (alreadyLiked) {
            // Unlike: Xóa bản ghi UserRecipeLike
            UserRecipeLike userRecipeLike = this.userRecipeLikeService.findByUserAndRecipe(recipe, currentUser);
            this.userRecipeLikeService.deleteUserRecipeLike(userRecipeLike);
            recipe.setLikeCount(recipe.getLikeCount() != null && recipe.getLikeCount() > 0 ? recipe.getLikeCount() - 1 : 0);
        } else {
            // Like: Tạo bản ghi UserRecipeLike
            UserRecipeLike userRecipeLike = UserRecipeLike.builder()
                    .user(currentUser)
                    .recipe(recipe)
                    .build();
            this.userRecipeLikeService.saveUserRecipeLike(userRecipeLike);
            recipe.setLikeCount(recipe.getLikeCount() != null ? recipe.getLikeCount() + 1 : 1);
        }

        // Lưu recipe với likeCount mới
        this.recipeService.saveRecipe(recipe);

        // Trả về response với likeCount và trạng thái likedByCurrentUser
        return ResponseEntity.ok(new LikeResponse(recipe.getLikeCount(), !alreadyLiked));
    }


    @GetMapping("/searchTitleName")
    public ResponseEntity<List<RecipeDetailDto>> searchRecipesByTitle(@RequestParam("keyword") String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<Recipe> recipes = this.recipeService.findTop4ByTileNameLike(keyword);
        List<RecipeDetailDto> dtos = recipes.stream().map(recipe -> {
            List<RecipeIngredientDTO> ingredientDtos = recipe.getRecipeIngredients() != null
                    ? recipe.getRecipeIngredients().stream().map(ri -> RecipeIngredientDTO.builder()
                    .id(ri.getId())
                    .ingredient(ri.getIngredient() != null
                            ? new IngredientDTO(ri.getIngredient().getId(), ri.getIngredient().getName())
                            : null)
                    .quantity(ri.getQuantity() != null ? ri.getQuantity().doubleValue() : 0.0)
                    .unit(ri.getUnit() != null ? ri.getUnit().toString() : "")
                    .build()).toList()
                    : Collections.emptyList();

            List<InstructionDTO> instructionDtos = recipe.getInstructions() != null
                    ? recipe.getInstructions().stream().map(i -> InstructionDTO.builder()
                    .id(null) // Instruction không có id trong định nghĩa hiện tại
                    .description(i.getDescription() != null ? i.getDescription() : "")
                    .step(i.getStep() != null ? i.getStep().intValue() : 0)
                    .imageUrl(i.getImageUrl() != null ? i.getImageUrl() : "")
                    .build()).toList()
                    : Collections.emptyList();

            return RecipeDetailDto.builder()
                    .id(recipe.getId())
                    .title(recipe.getTitle())
                    .tileName(recipe.getTileName())
                    .imageUrl(recipe.getImageUrl())
                    .preparationTime(recipe.getPreparationTime() != null ? recipe.getPreparationTime() : 0)
                    .cookingTime(recipe.getCookingTime() != null ? recipe.getCookingTime() : 0)
                    .servingSize(recipe.getServingSize() != null ? recipe.getServingSize() : 1)
                    .difficulty(recipe.getCategory() != null ? recipe.getCategory().getDifficulty() != null ? recipe.getCategory().getDifficulty().toString() : "Chưa xác định" : "Chưa xác định") // Thêm độ khó
                    .mealType(recipe.getCategory() != null ? recipe.getCategory().getMealType() != null ? recipe.getCategory().getMealType().toString() : "Chưa xác định" : "Chưa xác định") // Thêm bữa ăn
                    .recipeIngredients(ingredientDtos)
                    .instructions(instructionDtos)
                    .likeCount(recipe.getLikeCount() != null ? recipe.getLikeCount() : 0)
                    .totalCalories(recipe.getTotalCalories() != null ? recipe.getTotalCalories() : 0.0f)
                    .totalProtein(recipe.getTotalProtein() != null ? recipe.getTotalProtein() : 0.0f)
                    .totalFat(recipe.getTotalFat() != null ? recipe.getTotalFat() : 0.0f)
                    .totalCarbohydrates(recipe.getTotalCarbohydrates() != null ? recipe.getTotalCarbohydrates() : 0.0f)
                    .build();
        }).toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/myRecipe")
    public String getRecipeUser(Model model) {
        User user = this.userService.getCurrentUser();
        List<Recipe> recipes = this.recipeService.findByUserId(user.getId());
        model.addAttribute("recipes",recipes);
        return "client/Recipe/myRecipe";
    }





}

