package com.example.ecofood.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String title;

    String description;

    Integer preparationTime;

    Integer cookingTime;

    Integer servingSize;

    String imageUrl;


    LocalDate createdDate = LocalDate.now();

    LocalDate updatedDate;

    boolean related;

    Long parent_id;

    String tileName;

    Boolean isPendingRecipe = true;

    Float totalCalories = 0.0f;

    Float totalProtein = 0.0f;

    Float totalFat = 0.0f;

    Float totalCarbohydrates = 0.0f;

    @ElementCollection
    Set<Instruction> instructions = new HashSet<>(); // Initialized

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "cookSnap_id")
    CookSnap cookSnap;

    @ManyToMany(mappedBy = "recipes")
    Set<CookUtensil> cookUtensils = new HashSet<>();

    @OneToMany(mappedBy = "recipe")
    Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "recipe")
    Set<Audio> audios = new HashSet<>();

    @ManyToMany(mappedBy = "recipes")
    Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<RecipeIngredient> recipeIngredients = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "collection_id")
    Collection collection;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDate.now();
    }
}