package com.example.ecofood.service;


import com.example.ecofood.domain.Ingredient;
import com.example.ecofood.repository.IngredientRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IngredientService {


    IngredientRepository ingredientRepository;

    public Page<Ingredient> getAllIngredients(Pageable pageable) {
        return ingredientRepository.findAll(pageable);
    }

    public Page<Ingredient> searchIngredients(String name, String description, Pageable pageable) {
        if (name == null) name = "";
        if (description == null) description = "";
        return ingredientRepository.findByNameContainingAndDescriptionContaining(name, description, pageable);
    }

    public  Ingredient findAllById(Long id){
        return this.ingredientRepository.findAllById(id);
    }
    public List<Ingredient> findAll(){
        return this.ingredientRepository.findAll();
    }

    public long getTotalIngredients() {
        return ingredientRepository.count();
    }

    public void saveIngredient(Ingredient ingredient) {
        ingredientRepository.save(ingredient);
    }

    public void updateIngredient(Ingredient ingredient) {
        Ingredient ingredient1 = this.findAllById(ingredient.getId());
        ingredient1.setName(ingredient.getName());
        ingredient1.setDescription(ingredient.getDescription());
        ingredient1.setCaloriesPer100g(ingredient.getCaloriesPer100g());
        ingredient1.setCarbohydratesPer100g(ingredient.getCarbohydratesPer100g());
        ingredient1.setFatPer100g(ingredient.getFatPer100g());
        ingredient1.setProteinPer100g(ingredient.getProteinPer100g());

        this.ingredientRepository.save(ingredient1);

    }

    public List<Ingredient> findAllByNameContaining(String name){
        return this.ingredientRepository.findAllByNameContaining(name);
    }

    public void deleteIngredient(Long id) {
        ingredientRepository.deleteById(id);
    }

    public Ingredient findAllByName(String name){
        return this.ingredientRepository.findAllByName(name);
    }
}
