package com.example.ecofood.domain.DTO;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InstructionDTO {
    Long id;
    String description;
    Integer step;
    String imageUrl;
}