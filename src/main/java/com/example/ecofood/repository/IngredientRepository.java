package com.example.ecofood.repository;

import com.example.ecofood.domain.Ingredient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    Page<Ingredient> findByNameContainingAndDescriptionContaining(String name, String description, Pageable pageable);

    Ingredient findAllByName(String name);
    Ingredient findAllById(Long id);
}