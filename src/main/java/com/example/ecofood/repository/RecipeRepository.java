package com.example.ecofood.repository;

import com.example.ecofood.domain.Recipe;
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

    List<Recipe> findTop4ByTileNameContainingIgnoreCase(String keyword);

}
