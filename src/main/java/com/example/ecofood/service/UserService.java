package com.example.ecofood.service;

import com.example.ecofood.DTO.UserDTO;
import com.example.ecofood.Util.AppContext;
import com.example.ecofood.auth.JwtUtil;
import com.example.ecofood.domain.Category;
import com.example.ecofood.domain.User;
import com.example.ecofood.domain.UserActivity;
import com.example.ecofood.domain.UserSetting;
import com.example.ecofood.repository.UserActivityRepository;
import com.example.ecofood.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    UserSettingService userSettingService;
    PasswordEncoder passwordEncoder;
    UserActivityRepository userActivityRepository;
    @Value("${passWord.mail}")
    String passWordEmail ;
    AppContext appContext;


    public User findUserByUserName(String email) {
        return this.userRepository.findUsersByEmail(email);
    }

    public void createUser(UserDTO userDTO) {
        String passWordHash = this.passwordEncoder.encode(userDTO.getPasswordHash());

        // tao role
        User.Role role = User.Role.CUSTOMER;

        if (!appContext.isHasAdmin() && userDTO.getUserName().equals("admin")) {
            role = User.Role.ADMIN;
            appContext.setHasAdmin(true);
        }

        // Tạo người dùng mới
        User user = User.builder()
                .userName(userDTO.getUserName())
                .email(userDTO.getEmail())
                .passwordHash(passWordHash)
                .joinDate(userDTO.getJoinDate())
                .premium(false)
                .role(role)
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

    public boolean authenticate(String email, String passWord) {
        User user = this.userRepository.findUsersByEmail(email);

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
        this.userRepository.deleteById(id);
    }

    public User getCurrentUser()  {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        return null;
    }

    public User findByEmail(String email){
        return this.userRepository.findUsersByEmail(email);
    }
    public User createOAuth2User(String email, String name,String picture) {


        User user = new User();
        user.setEmail(email);
        user.setUserName(name);
        user.setJoinDate(LocalDate.now());
        user.setRole(User.Role.CUSTOMER);
        String passWordHash = this.passwordEncoder.encode(passWordEmail);
        user.setPasswordHash(passWordHash); // random pass

        this.userRepository.save(user);


        UserSetting userSetting = UserSetting.builder()
                .user(user)
                .accent(UserSetting.Accent.HN_PHUONGTRANG)
                .voiceGender(UserSetting.Gender.MALE)
                .region(Category.Region.NORTH)
                .urlImage(picture)
                .build();

        this.userSettingService.CreateSetting(userSetting);

        UserActivity userActivity = UserActivity.builder()
                .user(user)
                .build();

        this.userActivityRepository.save(userActivity);
        this.saveUser(user);
        return user;
    }

    public UserSetting.Accent setAccent( UserSetting.Gender gender , UserSetting.Region region){

        if (gender.equals(UserSetting.Gender.MALE)) {
            switch (region) {
                case SOUTH:
                    return UserSetting.Accent.HCM_MINHQUAN;
                case CENTRAL:
                    return UserSetting.Accent.HUE_BAOQUOC;
                default:
                    return UserSetting.Accent.HN_TIENQUAN;
            }
        }else {
            switch (region) {
                case SOUTH:
                    return UserSetting.Accent.HCM_DIEMMY;
                case CENTRAL:
                    return UserSetting.Accent.HUE_MAINGOC;
                default:
                    return UserSetting.Accent.HN_THANHHA;
            }
        }

    }

    public Boolean isNewPassWord(User user){
        return  this.passwordEncoder.matches(this.passWordEmail, user.getPasswordHash());
    }

    public void setPassWordNewUser(String pass){
        User user = this.getCurrentUser();
        user.setPasswordHash(this.passwordEncoder.encode(pass));
        this.userRepository.save(user);
    }

    public List<User> findByRoleName(String roleName){
        return  this.userRepository.findByRole(User.Role.valueOf(roleName));
    }

    public Boolean isEmailExist(String email){
        User user = findByEmail(email);
        return user == null? false:true;
    }

    public Boolean checkIfAdminExists(){
        return userRepository.existsByRole(User.Role.ADMIN);
    }
}
