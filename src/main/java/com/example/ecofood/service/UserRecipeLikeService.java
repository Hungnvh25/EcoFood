package com.example.ecofood.service;

import com.example.ecofood.domain.Recipe;
import com.example.ecofood.domain.User;
import com.example.ecofood.domain.UserRecipeLike;
import com.example.ecofood.repository.RecipeRepository;
import com.example.ecofood.repository.UserRecipeLikeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserRecipeLikeService {


    UserRecipeLikeRepository userRecipeLikeRepository;
    UserService userService;
    public Recipe getUserLikeRecipe(Recipe recipe) {

        User currentUser = this.userService.getCurrentUser();
        recipe.setLikedByCurrentUser(userRecipeLikeRepository.existsByUserAndRecipe(currentUser, recipe));
        return recipe;
    }

    public Boolean existsByUserAndRecipe(Recipe recipe,User user){
       return this.userRecipeLikeRepository.existsByUserAndRecipe(user,recipe);
    }

    public UserRecipeLike findByUserAndRecipe(Recipe recipe, User user){
        return  this.userRecipeLikeRepository.findByUserAndRecipe(user,recipe);
    }

    public void deleteUserRecipeLike(UserRecipeLike userRecipeLike){
        this.userRecipeLikeRepository.delete(userRecipeLike);
    }

    public void saveUserRecipeLike(UserRecipeLike userRecipeLike){
        this.userRecipeLikeRepository.save(userRecipeLike);
    }

    public void deleteByRecipeId(Long recipeId){
        this.userRecipeLikeRepository.deleteByRecipeId(recipeId);
    }
}
