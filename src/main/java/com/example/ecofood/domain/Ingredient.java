package com.example.ecofood.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    String description;

    String urlImage;

    Float avgG;

    Float caloriesPer100g;

    Float proteinPer100g;

    Float fatPer100g;

    Float carbohydratesPer100g;


    @OneToMany(mappedBy = "ingredient")
    Set<RecipeIngredient> recipeIngredients = new HashSet<>();




}
