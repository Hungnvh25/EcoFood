package com.example.ecofood.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeDetailDto {
    private Long id;
    private String title;
    private String tileName;
    private String difficulty;
    private String mealType;
    private String imageUrl;
    private Integer preparationTime;
    private Integer cookingTime;
    private Integer servingSize;
    private List<RecipeIngredientDTO> recipeIngredients;
    private List<InstructionDTO> instructions;
    private Integer likeCount;
    private Float totalCalories;
    private Float totalProtein;
    private Float totalFat;
    private Float totalCarbohydrates;
}
