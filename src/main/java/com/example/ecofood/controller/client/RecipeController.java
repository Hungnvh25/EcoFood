package com.example.ecofood.controller.client;

import com.example.ecofood.DTO.*;
import com.example.ecofood.domain.*;
import com.example.ecofood.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
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
    CommentService commentService;


    @GetMapping ("/")
    public String getEcoFood(Model model, HttpServletRequest request){


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
    public String showCreateForm(Model model,HttpServletRequest request) {
        Recipe recipe = new Recipe();
        User currentUser = this.userService.getCurrentUser();

        model.addAttribute("currentUser",currentUser);
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

        this.recipeService.createRecipe(recipe,imageFile,ingredientIds,ingredientQuantities,ingredientUnits,instructionDescriptions,instructionImages,difficulty,mealType);

        return "redirect:/recipe";
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

    @GetMapping("/recipe/{id}/comments")
    public ResponseEntity<List<CommentResponse>> getCommentsByRecipe(@PathVariable Long id) {
        List<Comment> comments = this.commentService.getCommentsByRecipeId(id);
        List<CommentResponse> responses = comments.stream()
                .map(comment -> new CommentResponse(
                        comment.getContent(),
                        comment.getUser().getUserName(),
                        comment.getUser().getUserSetting().getUrlImage(),
                        comment.getCreatedDate().toString()
                ))
                .toList();

        return ResponseEntity.ok(responses);
    }
    @PostMapping("/recipe/{id}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long id, @RequestBody CommentRequest request) {
        User currentUser = userService.getCurrentUser();
        Recipe recipe = recipeService.getRecipeById(id);

        Comment comment = Comment.builder()
                .content(request.getContent())
                .user(currentUser)
                .recipe(recipe)
                .createdDate(LocalDate.now())
                .build();

        this.commentService.saveComment(comment);

        return ResponseEntity.ok(new CommentResponse(
                comment.getContent(),
                currentUser.getUserName(),
                currentUser.getUserSetting().getUrlImage(),
                comment.getCreatedDate().toString()
        ));
    }

}

