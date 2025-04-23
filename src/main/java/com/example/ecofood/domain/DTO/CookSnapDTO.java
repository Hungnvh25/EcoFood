package com.example.ecofood.domain.DTO;

import com.example.ecofood.domain.Recipe;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class CookSnapDTO {

    Long id;

    String content;
    String urlImage;

    Long userId;
    private List<Recipe> recipes;
}
