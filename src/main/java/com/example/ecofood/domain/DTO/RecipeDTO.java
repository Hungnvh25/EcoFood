package com.example.ecofood.domain.DTO;

import com.example.ecofood.domain.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecipeDTO {
    Long id;
    String title;
    String description;
    Integer preparationTime;
    Integer cookingTime;
    Integer servingSize;
    String imageUrl;
    String difficulty;
    String mealType;
    LocalDate createdDate;
    LocalDate updatedDate;
    Float totalCalories;
    Float totalProtein;
    Float totalFat;
    Float totalCarbohydrates;

    Set<InstructionDTO> instructions = new HashSet<>();
    Long userId;
    Long cookSnapId;
    Set<RecipeIngredientDTO> recipeIngredients = new HashSet<>();
    Long collectionId;

    // Thêm các thuộc tính cần thiết khác mà không gây vòng lặp
    // Ví dụ: chỉ lấy id của các đối tượng liên quan
    Set<Long> cookUtensilIds = new HashSet<>();
    Set<Long> categoryIds = new HashSet<>();
}
