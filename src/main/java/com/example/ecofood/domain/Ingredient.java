package com.example.ecofood.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

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

    @NotNull(message = "Name không được để trống")
    @Size(min = 2, max = 100, message = "Name phải từ 2 đến 100 ký tự")
    String name;

    @Size(max = 500, message = "Description không được vượt quá 500 ký tự")
    String description;

    String urlImage;

    @NotNull(message = "Calories không được để trống")
    @DecimalMin(value = "0.0", message = "Calories phải lớn hơn hoặc bằng 0")
    Float caloriesPer100g;

    @NotNull(message = "Protein không được để trống")
    @DecimalMin(value = "0.0", message = "Protein phải lớn hơn hoặc bằng 0")
    Float proteinPer100g;

    @NotNull(message = "Fat không được để trống")
    @DecimalMin(value = "0.0", message = "Fat phải lớn hơn hoặc bằng 0")
    Float fatPer100g;

    @NotNull(message = "Carbohydrates không được để trống")
    @DecimalMin(value = "0.0", message = "Carbohydrates phải lớn hơn hoặc bằng 0")
    Float carbohydratesPer100g;


    @ManyToMany
    @JoinTable(
            name = "recipe_ingredient",
            joinColumns = @JoinColumn(name = "ingredient_id"),
            inverseJoinColumns = @JoinColumn(name = "recipe_id")
    )
    private Set<Recipe> recipes;

    @ManyToMany
    @JoinTable(
            name = "ingredient_allergen",
            joinColumns = @JoinColumn(name = "ingredient_id"),
            inverseJoinColumns = @JoinColumn(name = "allergen_id")
    )
    private Set<Allergen> allergens;

}
