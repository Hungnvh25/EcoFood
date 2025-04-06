package com.example.ecofood.service;


import com.example.ecofood.domain.User;
import com.example.ecofood.domain.UserSetting;
import com.example.ecofood.repository.UserSettingRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserSettingService {

    UserSettingRepository userSettingRepository;

    public void createUserSetting(User user) {
        UserSetting userSetting = UserSetting.builder()
                .user(user)
                .accent(UserSetting.Accent.HN_PHUONGTRANG)
                .voiceGender(UserSetting.Gender.MALE)
                .build();
        this.userSettingRepository.save(userSetting);
    }

}
