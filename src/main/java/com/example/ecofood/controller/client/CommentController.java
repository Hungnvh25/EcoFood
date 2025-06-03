package com.example.ecofood.controller.client;

import com.example.ecofood.DTO.CommentRequest;
import com.example.ecofood.DTO.CommentResponse;
import com.example.ecofood.domain.Comment;
import com.example.ecofood.domain.Recipe;
import com.example.ecofood.domain.User;
import com.example.ecofood.service.CommentService;
import com.example.ecofood.service.RecipeService;
import com.example.ecofood.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentController {

    CommentService commentService;
    UserService userService;
    RecipeService recipeService;
    @GetMapping("/recipe/{id}/comments")
    public ResponseEntity<List<CommentResponse>> getCommentsByRecipe(@PathVariable Long id) {
        List<Comment> comments = this.commentService.getCommentsByRecipeId(id);
        List<CommentResponse> responses = comments.stream()
                .map(comment -> new CommentResponse(
                        comment.getContent(),
                        comment.getUser().getUserName(),
                        comment.getUser().getUserSetting().getUrlImage(),
                        comment.getCreatedDate().toString()
                ))
                .toList();

        return ResponseEntity.ok(responses);
    }
    @PostMapping("/recipe/{id}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long id, @RequestBody CommentRequest request) {
        User currentUser = userService.getCurrentUser();
        Recipe recipe = recipeService.getRecipeById(id);

        Comment comment = Comment.builder()
                .content(request.getContent())
                .user(currentUser)
                .recipe(recipe)
                .createdDate(LocalDate.now())
                .build();

        this.commentService.saveComment(comment);

        return ResponseEntity.ok(new CommentResponse(
                comment.getContent(),
                currentUser.getUserName(),
                currentUser.getUserSetting().getUrlImage(),
                comment.getCreatedDate().toString()
        ));
    }
}
