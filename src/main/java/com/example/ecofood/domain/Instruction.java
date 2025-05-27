package com.example.ecofood.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Instruction {

    Long step;

    @Lob
    String description;

    String imageUrl;

}