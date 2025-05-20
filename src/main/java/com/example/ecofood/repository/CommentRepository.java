package com.example.ecofood.repository;

import com.example.ecofood.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByRecipeIdOrderByCreatedDateDesc(Long recipeId);

}
