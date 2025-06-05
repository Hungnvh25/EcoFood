package com.example.ecofood.domain;

import jakarta.persistence.*;
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
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    @OneToOne
    @JoinColumn(name = "recipe_id", referencedColumnName = "id")
    private Recipe recipe;

    @Enumerated(EnumType.STRING)
    Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    MealType mealType;

    @Enumerated(EnumType.STRING)
    Region region;

    public enum Difficulty {
        EASY("Dễ"),
        MEDIUM("Trung bình"),
        HARD("Khó");

        private String description;

        Difficulty(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum MealType {
        BREAKFAST("Bữa sáng"),
        LUNCH("Bữa trưa"),
        DINNER("Bữa tối"),
        SNACK("Bữa phụ"),
        MAIN_COURSE("Bữa chính");

        private String description;

        MealType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum Region {
        NORTH("Miền Bắc"),
        CENTRAL("Miền Trung"),
        SOUTH("Miền Nam");

        private String description;

        Region(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
