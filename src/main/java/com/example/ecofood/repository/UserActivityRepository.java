package com.example.ecofood.repository;

import com.example.ecofood.domain.Recipe;
import com.example.ecofood.domain.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

    @Query("SELECT r FROM UserActivity ua JOIN ua.recipeIds r WHERE ua.user.id = :userId")
    List<Long> findAllRecipesByUser_Id(@Param("userId") Long userId);
    void deleteByRecipeIdsContaining(Long recipeId); // ✅ Đúng
    List<UserActivity> findAllByRecipeIds(Long id);

}
