package com.example.ecofood.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommentResponse {
    private String content;
    private String userName;
    private String userImage;
    private String createdDate;
}