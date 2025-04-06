package com.example.ecofood.domain.DTO;

import com.example.ecofood.domain.Recipe;
import com.example.ecofood.domain.User;
import com.example.ecofood.domain.UserSetting;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDTO {

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
    User.Role role;

    Long userSettingId;



    public enum Role {
        CUSTOMER,
        ADMIN
    }
}
