package com.example.ecofood.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecipeIngredient {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    Recipe recipe;

    @ManyToOne
    @JoinColumn(name = "ingredient_id")
    Ingredient ingredient;


    Float quantity;

    @Enumerated(EnumType.STRING)
    Unit unit;

    public enum Unit {
        GRAM,
        ML,
        PIECE,
        TABLESPOON,
        TEASPOON
    }

}
