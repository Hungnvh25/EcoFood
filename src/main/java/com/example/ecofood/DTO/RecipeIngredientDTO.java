package com.example.ecofood.DTO;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecipeIngredientDTO {
    private Long id;
    private IngredientDTO ingredient;
    private Double quantity;
    private String unit;
}