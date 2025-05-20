package com.example.ecofood.service;

import com.example.ecofood.domain.Comment;
import com.example.ecofood.repository.CommentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentService {
    CommentRepository commentRepository;

    public List<Comment> getCommentsByRecipeId(Long recipeId) {
        return commentRepository.findByRecipeIdOrderByCreatedDateDesc(recipeId);
    }

    public void saveComment(Comment comment){
        this.commentRepository.save(comment);
    }
}
