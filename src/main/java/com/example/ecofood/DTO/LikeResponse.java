package com.example.ecofood.DTO;

public class LikeResponse {
    private Integer likeCount;
    private boolean likedByCurrentUser;

    public LikeResponse(Integer likeCount, boolean likedByCurrentUser) {
        this.likeCount = likeCount;
        this.likedByCurrentUser = likedByCurrentUser;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public boolean isLikedByCurrentUser() {
        return likedByCurrentUser;
    }
}
