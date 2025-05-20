package com.example.ecofood.repository;

import com.example.ecofood.domain.Recipe;
import com.example.ecofood.domain.User;
import com.example.ecofood.domain.UserRecipeLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRecipeLikeRepository extends JpaRepository<UserRecipeLike, Long> {
    boolean existsByUserAndRecipe(User user, Recipe recipe);
    UserRecipeLike findByUserAndRecipe(User user, Recipe recipe);
}