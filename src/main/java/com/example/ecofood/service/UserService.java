package com.example.ecofood.service;

import com.example.ecofood.DTO.UserDTO;
import com.example.ecofood.domain.User;
import com.example.ecofood.domain.UserActivity;
import com.example.ecofood.repository.UserActivityRepository;
import com.example.ecofood.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserSettingService userSettingService;
    PasswordEncoder passwordEncoder;
    HttpServletRequest request;
    UserActivityRepository userActivityRepository;

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

        UserActivity userActivity = UserActivity.builder()
                .user(user)
                .build();

        this.userActivityRepository.save(userActivity);
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


    public Page<User> getAllUsers(Pageable pageable) {
        return this.userRepository.findAll(pageable);
    }

    public Page<User> searchUsers(String userName, String email, Pageable pageable) {
        if (userName == null) userName = "";
        if (email == null) email = "";
        return this.userRepository.findByUserNameContainingAndEmailContaining(userName, email, pageable);
    }

    public long getTotalUsers() {
        return this.userRepository.count();
    }

    public User findUserById(Long id) {
        return this.userRepository.findById(id).orElse(null);
    }

    public void deleteUser(Long id) {
        User user = findUserById(id);
        this.userRepository.deleteById(id);
    }

    public User getCurrentUser(){
        User user = (User) this.request.getAttribute("user");
        if (user!=null){
            return user;
        }
         return null;
    }

    public User findByEmail(String email){
        return this.userRepository.findUsersByEmail(email);
    }
    public User createOAuth2User(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setUserName(generateUserNameFromEmail(email));
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString())); // random pass
        user.setJoinDate(LocalDate.now());
        user.setRole(User.Role.CUSTOMER);
        userRepository.save(user);

        this.userSettingService.createUserSetting(user);

        UserActivity userActivity = UserActivity.builder()
                .user(user)
                .build();

        this.userActivityRepository.save(userActivity);
        return user;
    }

    public String generateUserNameFromEmail(String email) {
        return email.split("@")[0] + "_" + UUID.randomUUID().toString().substring(0, 5);
    }



}
