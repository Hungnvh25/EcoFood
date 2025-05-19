package com.example.ecofood.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ElementCollection(fetch = FetchType.EAGER)
    List<Long> recipeIds = new ArrayList<>();


    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
