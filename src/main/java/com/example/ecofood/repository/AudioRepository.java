package com.example.ecofood.repository;

import com.example.ecofood.domain.Audio;
import com.example.ecofood.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AudioRepository extends JpaRepository<Audio, Long> {
}
