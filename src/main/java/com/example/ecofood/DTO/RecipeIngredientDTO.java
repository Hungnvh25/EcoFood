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
    Long id;
    Long ingredientId;
    String ingredientName;
    Float quantity;
    String unit;
}