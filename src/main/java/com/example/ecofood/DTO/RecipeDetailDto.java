package com.example.ecofood.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeDetailDto {
    private Long id;
    private String title;
    private String tileName;
    private String description;
    private Integer preparationTime;
    private Integer cookingTime;
    private Integer servingSize;
    private String imageUrl;
    private Integer likeCount;
    private LocalDate createdDate;
    private LocalDate updatedDate;
    private Boolean isPendingRecipe;
    private Float totalCalories;
    private Float totalProtein;
    private Float totalFat;
    private Float totalCarbohydrates;
    private List<RecipeIngredientDTO> recipeIngredients;
    private List<InstructionDTO> instructions;
    private Boolean likedByCurrentUser;
    private String region;
}
