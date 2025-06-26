package com.example.ecofood.repository;

import com.example.ecofood.domain.Category;
import com.example.ecofood.domain.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByTitleContainingIgnoreCase(String keyword);
    Optional<Recipe> findById(Long id);

    List<Recipe> findTop3ByIsPendingRecipeFalseOrderByLikeCountDesc();

    List<Recipe> findTop5ByTitleContainingIgnoreCase(String keyword);


    Page<Recipe> findByIsPendingRecipeFalse(Pageable pageable);

    Page<Recipe> findByIsPendingRecipeTrue(Pageable pageable);

    List<Recipe> findByIsPendingRecipeFalse();

    Page<Recipe> findByTitleContainingIgnoreCaseAndIsPendingRecipeFalse(String title, Pageable pageable);

    Page<Recipe> findByUserUserNameContainingIgnoreCaseAndIsPendingRecipeFalse(String userName, Pageable pageable);

    Page<Recipe> findByTitleContainingIgnoreCaseAndUserUserNameContainingIgnoreCaseAndIsPendingRecipeFalse(String title, String userName, Pageable pageable);

    Page<Recipe> findByTitleContainingIgnoreCaseAndIsPendingRecipeTrue(String title, Pageable pageable);

    Page<Recipe> findByUserUserNameContainingIgnoreCaseAndIsPendingRecipeTrue(String userName, Pageable pageable);

    Page<Recipe> findByTitleContainingIgnoreCaseAndUserUserNameContainingIgnoreCaseAndIsPendingRecipeTrue(String title, String userName, Pageable pageable);

    long countByIsPendingRecipeTrue();

    long countByIsPendingRecipeFalse();

    @Query("SELECT COALESCE(SUM(r.likeCount), 0) FROM Recipe r")
    long sumLikeCount();

    List<Recipe> findByUserId(Long id);

    List<Recipe> findByIsPendingRecipeNullAndUserId(Long userId);


    @Query("SELECT r FROM Recipe r WHERE " +
            "LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%')) AND " +
            "(:difficulty IS NULL OR r.category.difficulty = :difficulty) AND " +
            "(:mealType IS NULL OR r.category.mealType = :mealType) AND " +
            "(:region IS NULL OR r.category.region = :region)")
    List<Recipe> findByTitleContainingIgnoreCaseAndCategoryDifficultyAndCategoryMealTypeAndCategoryRegion(
            @Param("title") String title,
            @Param("difficulty") Category.Difficulty difficulty,
            @Param("mealType") Category.MealType mealType,
            @Param("region") Category.Region region
    );

    List<Recipe> findByParentId(Long parentId);

    List<Recipe> findRecipeByIsPendingRecipeFalse();


}
