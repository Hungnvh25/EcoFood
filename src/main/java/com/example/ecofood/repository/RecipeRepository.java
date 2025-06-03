package com.example.ecofood.repository;

import com.example.ecofood.domain.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    List<Recipe> findTop3ByOrderByLikeCountDesc();

    List<Recipe> findTop5ByTitleContainingIgnoreCase(String keyword);

    Page<Recipe> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Recipe> findByUserUserNameContainingIgnoreCase(String userName, Pageable pageable);

    Page<Recipe> findByTitleContainingIgnoreCaseAndUserUserNameContainingIgnoreCase(String title, String userName, Pageable pageable);

    Page<Recipe> findByIsPendingRecipeFalse(Pageable pageable);

    Page<Recipe> findByIsPendingRecipeTrue(Pageable pageable);

    Page<Recipe> findByTitleContainingIgnoreCaseAndIsPendingRecipeFalse(String title, Pageable pageable);

    Page<Recipe> findByUserUserNameContainingIgnoreCaseAndIsPendingRecipeFalse(String userName, Pageable pageable);

    Page<Recipe> findByTitleContainingIgnoreCaseAndUserUserNameContainingIgnoreCaseAndIsPendingRecipeFalse(String title, String userName, Pageable pageable);

    Page<Recipe> findByTitleContainingIgnoreCaseAndIsPendingRecipeTrue(String title, Pageable pageable);

    Page<Recipe> findByUserUserNameContainingIgnoreCaseAndIsPendingRecipeTrue(String userName, Pageable pageable);

    Page<Recipe> findByTitleContainingIgnoreCaseAndUserUserNameContainingIgnoreCaseAndIsPendingRecipeTrue(String title, String userName, Pageable pageable);

    long countByIsPendingRecipeTrue();

    long countByIsPendingRecipeFalse();

    @Query("SELECT SUM(r.likeCount) FROM Recipe r")
    long sumLikeCount();

    List<Recipe> findByUserId(Long id);

    @Query("SELECT r FROM Recipe r WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Recipe> findTopByTileNameContainingIgnoreCase(String title, Pageable pageable);

}
