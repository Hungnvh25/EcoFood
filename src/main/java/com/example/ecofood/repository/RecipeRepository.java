package com.example.ecofood.repository;

import com.example.ecofood.domain.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByTitleContainingIgnoreCase(String keyword);
    Optional<Recipe> findById(Long id);

    List<Recipe> findTop3ByOrderByLikeCountDesc();

    List<Recipe> findTop4ByTileNameContainingIgnoreCase(String keyword);



    Page<Recipe> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Recipe> findByUserUserNameContainingIgnoreCase(String userName, Pageable pageable);

    Page<Recipe> findByTitleContainingIgnoreCaseAndUserUserNameContainingIgnoreCase(String title, String userName, Pageable pageable);

    Page<Recipe> findByIsPendingRecipeFalse(Pageable pageable); // Thêm: Lấy công thức đã duyệt

    Page<Recipe> findByTitleContainingIgnoreCaseAndIsPendingRecipeFalse(String title, Pageable pageable); // Thêm: Tìm kiếm theo tiêu đề, đã duyệt

    Page<Recipe> findByUserUserNameContainingIgnoreCaseAndIsPendingRecipeFalse(String userName, Pageable pageable); // Thêm: Tìm kiếm theo người tạo, đã duyệt

    Page<Recipe> findByTitleContainingIgnoreCaseAndUserUserNameContainingIgnoreCaseAndIsPendingRecipeFalse(String title, String userName, Pageable pageable); // Thêm: Tìm kiếm kết hợp, đã duyệt

    long countByIsPendingRecipeTrue();

    long countByIsPendingRecipeFalse(); // Thêm: Đếm công thức đã duyệt

    @Query("SELECT SUM(r.likeCount) FROM Recipe r")
    long sumLikeCount();

}
