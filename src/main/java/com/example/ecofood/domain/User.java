package com.example.ecofood.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotNull(message = "UserName not null")
//    @Size(min = 3, max = 50, message = "Username phải từ 3 đến 50 ký tự")
//    @Pattern(regexp = "[A-Za-z0-9_]+", message = "Username chỉ được chứa chữ cái, số và dấu gạch dưới")
    String userName;


    @Email(message = "Email is not valid")
    @NotEmpty(message = "Email cannot be empty")
    String email;

    @NotEmpty(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    String passwordHash;


    LocalDate joinDate = LocalDate.now();
    Boolean premium = false;
    LocalDateTime premiumStartDate;

    @Enumerated(EnumType.STRING)
    Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserSetting userSetting;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserActivity userActivity;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SaveRecipe> saveRecipes = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Notification> notifications = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Collection collection;

    @OneToMany(mappedBy = "user")
    private List<Recipe> recipes;

    @OneToMany(mappedBy = "user")
    private List<CookSnap> cookSnaps;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<UserRecipeLike> likedRecipes = new HashSet<>();


    public enum Role {
        CUSTOMER,
        ADMIN
    }


}
