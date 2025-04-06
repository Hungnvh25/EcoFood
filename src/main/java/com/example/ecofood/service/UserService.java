package com.example.ecofood.service;

import com.example.ecofood.domain.DTO.UserDTO;
import com.example.ecofood.domain.User;
import com.example.ecofood.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserSettingService userSettingService;
    PasswordEncoder passwordEncoder;

    public User findUserByUserName(String userName) {
        return this.userRepository.findUserByUserName(userName);
    }

    public void createUser(UserDTO userDTO) {
        String passWordHash = this.passwordEncoder.encode(userDTO.getPasswordHash());
        User user = User.builder()
                .userName(userDTO.getUserName())
                .email(userDTO.getEmail())
                .passwordHash(passWordHash)
                .joinDate(userDTO.getJoinDate())
                .premium(false)
                .role(User.Role.CUSTOMER)
                .build();

        this.userRepository.save(user);
        this.userSettingService.createUserSetting(user);
    }

    public List<User> findAllUser(){
        return this.userRepository.findAll();

    }

    public void saveUser(User user){
        this.userRepository.save(user);
    }

    public boolean authenticate(String userName, String passWord) {
        User user = this.userRepository.findUserByUserName(userName);

        if (user == null) {
            return false;
        }
        return this.passwordEncoder.matches(passWord, user.getPasswordHash());
    }
}
