package com.example.ecofood.repository;

import com.example.ecofood.domain.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSettingRepository extends JpaRepository<UserSetting,Long> {
}
