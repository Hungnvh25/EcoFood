package com.example.ecofood.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CookSnap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String content;
    String urlImage;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    Recipe recipe;


}
