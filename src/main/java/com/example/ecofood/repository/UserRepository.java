package com.example.ecofood.repository;

import com.example.ecofood.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findUserByUserName(String userName);
    Page<User> findByUserNameContainingAndEmailContaining(String userName, String email, Pageable pageable);

    User findUsersByEmail(String email);

    public List<User> findByRole(User.Role role);
}
