package com.example.ecofood.repository;


import com.example.ecofood.domain.Recipe;
import com.example.ecofood.domain.SaveRecipe;
import com.example.ecofood.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SaveRecipeRepository extends JpaRepository<SaveRecipe, Long> {
    Optional<SaveRecipe> findByUserAndRecipe(User user, Recipe recipe);


    List<SaveRecipe> findByUserId(Long userId);

}