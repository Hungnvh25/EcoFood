package com.example.ecofood.domain;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
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

    Float totalCalories;

    Float totalProtein;

    Float totalFat;

    Float totalCarbohydrates;

    @ElementCollection
    Set<Instruction> instructions;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;


    @ManyToOne
    @JoinColumn(name = "cookSnap_id")
    private CookSnap cookSnap;

    @ManyToMany(mappedBy = "recipes")
    private Set<CookUtensil> cookUtensils;

    @OneToMany(mappedBy = "recipe")
    private Set<Comment> comments;

    @OneToMany(mappedBy = "recipe")
    private Set<Audio> audios;

    @ManyToMany(mappedBy = "recipes")
    private Set<Category> categories;

    @ManyToMany(mappedBy = "recipes")
    private Set<Ingredient> ingredients;

    @ManyToOne
    @JoinColumn(name = "collection_id")
    private Collection collection;


    @PrePersist
    protected void onCreate() {
        createdDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDate.now();
    }



}