package com.example.ecofood.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

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
    String userName;


    @Email(message = "Email is not valid")
    @NotEmpty(message = "Email cannot be empty")
    String email;

    @NotEmpty(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    String passwordHash;


    LocalDate joinDate = LocalDate.now();
    Boolean premium = false;

    @Enumerated(EnumType.STRING)
    Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserSetting userSetting;

    @OneToMany(mappedBy = "user")
    private List<Recipe> recipes;

    public enum Role {
        CUSTOMER,
        ADMIN
    }


}
