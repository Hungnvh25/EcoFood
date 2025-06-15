package com.example.ecofood.controller.client;

import com.example.ecofood.DTO.*;
import com.example.ecofood.domain.*;
import com.example.ecofood.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    NotificationService notificationService;



    @GetMapping("/")
    public String getEcoFood(Model model, HttpServletRequest request) {
        // Lấy danh sách recipes từ service
        List<Recipe> recipes = this.recipeService.getAllRecipesIsPendingFalse();
        recipes = this.recipeService.remoteRecipeListIsPremiumNull(recipes);
        model.addAttribute("recipes", recipes);

        return "index";
    }

    @GetMapping("/search")
    public String searchRecipes(
            @RequestParam("keyword") String keyword,
            @RequestParam(required = false) Category.Difficulty difficulty,
            @RequestParam(required = false) Category.MealType mealType,
            @RequestParam(required = false) Category.Region region,
            Model model) {

        List<Recipe> searchResults = this.recipeService.searchRecipesByTitleAndFilters(keyword, difficulty, mealType, region);

        model.addAttribute("searchResults", searchResults);
        model.addAttribute("keyword", keyword);
        model.addAttribute("difficulty", difficulty);
        model.addAttribute("mealType", mealType);
        model.addAttribute("region", region);

        // Truyền danh sách enum để hiển thị filter
        model.addAttribute("difficulties", Category.Difficulty.values());
        model.addAttribute("mealTypes", Category.MealType.values());
        model.addAttribute("regions", Category.Region.values());

        return "client/Search/search";
    }

    @GetMapping("/recipe")
    public String showCreateForm(@RequestParam(name = "recipeId", required = false) Long recipeId,
                                 Model model, HttpServletRequest request) {
        Recipe recipe;

        if (recipeId != null) {
            recipe = recipeService.getRecipeById(recipeId); // Lấy món ăn theo ID
        } else {
            recipe = new Recipe(); // Tạo mới nếu không có ID
        }

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
            @RequestParam(value = "region", required = true) String region,
            @RequestParam(value = "publish", defaultValue = "false") boolean publish,RedirectAttributes redirectAttributes) {

        try {
            String message ;
            if (publish) {
                if (result.hasErrors() || recipe.getTitle() == null || recipe.getTitle().isBlank() ||
                        recipe.getCookingTime() == null || recipe.getServingSize() == null ||
                        difficulty == null || mealType == null || region == null ||
                        ingredientIds == null || ingredientIds.isEmpty() ||
                        ingredientQuantities == null || ingredientQuantities.isEmpty() ||
                        ingredientUnits == null || ingredientUnits.isEmpty() ||
                        instructionDescriptions == null || instructionDescriptions.isEmpty()) {
                    return "client/Recipe/add";
                }
                recipe.setIsPendingRecipe(true);
                this.notificationService.createRecipePendingNotification(recipe);
                this.recipeService.createRecipe(recipe, imageFile, ingredientIds, ingredientQuantities, ingredientUnits,
                        instructionDescriptions, instructionImages, difficulty, mealType, region);
                message = "Món ăn của bạn đang được chờ duyệt";
            } else {
                // Gọi hàm lưu không kiểm tra bắt buộc khi không publish
                this.recipeService.createRecipeIsPendingNull(recipe, imageFile, ingredientIds, ingredientQuantities, ingredientUnits,
                        instructionDescriptions, instructionImages, difficulty, mealType, region);
                message = "Món ăn lưu thành công vào món nháp";

            }
            redirectAttributes.addFlashAttribute("success", message);

            return "redirect:/";
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("failed", "Có lỗi khi tạo món: " + e.getMessage());
            return "redirect:/";

        }

    }

    @GetMapping("/recipe/{id}")
    public String getRecipeDetail(@PathVariable Long id, Model model) {
        try {
            Recipe recipe = this.recipeService.getRecipeById(id);
            model.addAttribute("recipe", recipe);
            User user = this.userService.getCurrentUser();

            CookSnap userCookSnap = recipe.getCookSnaps().stream()
                    .filter(cookSnap -> cookSnap.getUser().getId().equals(user.getId()))
                    .findFirst().orElse(null);

            boolean hasUserCookSnap = recipe.getCookSnaps().stream()
                    .anyMatch(cookSnap -> cookSnap.getUser().getId().equals(user.getId()));
            model.addAttribute("hasUserCookSnap", hasUserCookSnap);
            model.addAttribute("userCookSnap", userCookSnap);


            if (recipe.getIsPendingRecipe()!=null){
                this.userActivityService.saveHistoryViewRecipe(recipe);
            }
            return "client/Recipe/recipe-detail";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", "Không tìm thấy món ăn với ID: " + id);
            return "error"; // Giả định bạn có template error.html
        }
    }


    @GetMapping("/recipeTest/{id}")
    public String getRecipeDetailTest(@PathVariable Long id, Model model) {
        try {
            Recipe recipe = this.recipeService.getRecipeById(id);
            model.addAttribute("recipe", recipe);
            User user = this.userService.getCurrentUser();

            CookSnap userCookSnap = recipe.getCookSnaps().stream()
                    .filter(cookSnap -> cookSnap.getUser().getId().equals(user.getId()))
                    .findFirst().orElse(null);

            boolean hasUserCookSnap = recipe.getCookSnaps().stream()
                    .anyMatch(cookSnap -> cookSnap.getUser().getId().equals(user.getId()));
            model.addAttribute("hasUserCookSnap", hasUserCookSnap);
            model.addAttribute("userCookSnap", userCookSnap);



            if (recipe.getIsPendingRecipe()!=null){
                this.userActivityService.saveHistoryViewRecipe(recipe);
            }
            return "client/Recipe/detail-recipeTest";
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

        List<RecipeDetailDto> dtos = recipes.stream()
                .map(this.recipeService::mapToRecipeDetailDto)
                .collect(Collectors.toList());

        dtos.removeIf(recipeDetailDto -> recipeDetailDto.getIsPendingRecipe() == null);
        return ResponseEntity.ok(dtos);
    }



    @GetMapping("/myRecipe")
    public String getRecipeUser(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "difficulty", required = false) Category.Difficulty difficulty,
            @RequestParam(name = "mealType", required = false) Category.MealType mealType,
            @RequestParam(name = "region", required = false) Category.Region region,
            Model model) {

        List<Recipe> recipes;
        User user = this.userService.getCurrentUser();

        if((keyword != null && !keyword.isEmpty()) ||
                (difficulty != null && !difficulty.name().isEmpty()) ||
                (mealType != null && !mealType.name().isEmpty()) ||
                (region != null && !region.name().isEmpty())){
            recipes = this.recipeService.searchRecipesByTitleAndFilters(keyword, difficulty, mealType, region);
            recipes = this.recipeService.searchRecipesByTitleAndFilterMyRecipe(recipes);
        }
        else {
            recipes = this.recipeService.findByUserId(user.getId());
        }

        recipes.removeIf(recipe -> recipe.getUser().getId() != user.getId());

        recipes = this.recipeService.remoteRecipeListIsPremiumNull(recipes);
        // Truyền dữ liệu sang view
        model.addAttribute("recipes", recipes);
        model.addAttribute("keyword", keyword);
        model.addAttribute("difficulty", difficulty);
        model.addAttribute("mealType", mealType);
        model.addAttribute("region", region);

        // Truyền danh sách enum để hiển thị filter
        model.addAttribute("difficulties", Category.Difficulty.values());
        model.addAttribute("mealTypes", Category.MealType.values());
        model.addAttribute("regions", Category.Region.values());

        return "client/Recipe/myRecipe";
    }



    @GetMapping("/recipeTest")
    public String getRecipeTest(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "difficulty", required = false) Category.Difficulty difficulty,
            @RequestParam(name = "mealType", required = false) Category.MealType mealType,
            @RequestParam(name = "region", required = false) Category.Region region,
            Model model) {

        List<Recipe> recipes;
        User user = this.userService.getCurrentUser();

        if((keyword != null && !keyword.isEmpty()) ||
                (difficulty != null && !difficulty.name().isEmpty()) ||
                (mealType != null && !mealType.name().isEmpty()) ||
                (region != null && !region.name().isEmpty())){
            recipes = this.recipeService.searchRecipesByTitleAndFilters(keyword, difficulty, mealType, region);
            recipes = this.recipeService.searchRecipesByTitleAndFilterMyRecipeTest(recipes);
        }
        else {
            recipes = this.recipeService.findRecipeByIsPendingRecipeNull(user.getId());
        }

        recipes.removeIf(recipe -> recipe.getUser().getId() != user.getId());


        // Truyền dữ liệu sang view
        model.addAttribute("recipes", recipes);
        model.addAttribute("keyword", keyword);
        model.addAttribute("difficulty", difficulty);
        model.addAttribute("mealType", mealType);
        model.addAttribute("region", region);

        // Truyền danh sách enum để hiển thị filter
        model.addAttribute("difficulties", Category.Difficulty.values());
        model.addAttribute("mealTypes", Category.MealType.values());
        model.addAttribute("regions", Category.Region.values());

        return "client/Recipe/recipeTest";
    }
    @PostMapping("/userrecipe/delete")
    public String deleteRecipe(@RequestParam("id") String id, RedirectAttributes redirectAttributes,
                               HttpServletRequest request) {

        try {

            if (id == null || id.isEmpty()) {
                redirectAttributes.addFlashAttribute("failed", "Yêu cầu ID công thức.");
                return "redirect:/recipeTest";
            }
            this.recipeService.deleteRecipe(Long.parseLong(id));
            redirectAttributes.addFlashAttribute("success", "Công thức đã được xóa thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("failed", "Không thể xóa công thức: " + e.getMessage());
        }
        return "redirect:/recipeTest";
    }

    @PostMapping("/myrecipe/delete")
    public String deleteMyRecipe(@RequestParam("id") String id, RedirectAttributes redirectAttributes,
                               HttpServletRequest request) {

        try {

            if (id == null || id.isEmpty()) {
                redirectAttributes.addFlashAttribute("failed", "Yêu cầu ID công thức.");
                return "redirect:/myRecipe";
            }
            this.recipeService.deleteRecipe(Long.parseLong(id));
            redirectAttributes.addFlashAttribute("success", "Công thức đã được xóa thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("failed", "Không thể xóa công thức: " + e.getMessage());
        }
        return "redirect:/myRecipe";
    }

}

