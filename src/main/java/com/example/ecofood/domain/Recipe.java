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
    @Lob
    String description;

    Integer preparationTime;

    Integer cookingTime;

    Integer servingSize;

    String imageUrl;

    Integer likeCount;

    LocalDate createdDate = LocalDate.now();

    LocalDate updatedDate;

    boolean related;

    Long parentId; // id recipe cha

    String tileName; // để check tên gần giống

    Boolean isPendingRecipe = true;  // chờ admin duyệt
    @Lob
    String textAudio ;

    Float totalCalories = 0.0f;

    Float totalProtein = 0.0f;

    Float totalFat = 0.0f;

    Float totalCarbohydrates = 0.0f;

    @Transient
    private boolean likedByCurrentUser;

    @ElementCollection
    Set<Instruction> instructions = new HashSet<>(); // Initialized

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "cookSnap_id")
    CookSnap cookSnap;


    @OneToMany(mappedBy = "recipe")
    Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "recipe")
    Set<SaveRecipe> saveRecipes = new HashSet<>();

    @OneToMany(mappedBy = "recipe",cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Audio> audios = new HashSet<>();

    @OneToOne(mappedBy = "recipe", cascade = CascadeType.ALL)
    Category category;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<RecipeIngredient> recipeIngredients = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "collection_id")
    Collection collection;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<UserRecipeLike> userLikes = new HashSet<>();


    @PrePersist
    protected void onCreate() {
        createdDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDate.now();
    }
}