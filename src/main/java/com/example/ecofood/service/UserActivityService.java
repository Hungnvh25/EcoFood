package com.example.ecofood.service;

import com.example.ecofood.domain.Recipe;
import com.example.ecofood.domain.User;
import com.example.ecofood.domain.UserActivity;
import com.example.ecofood.repository.UserActivityRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserActivityService {

    @Autowired
    UserService userService;
    @Autowired
    UserActivityRepository userActivityRepository;


    public List<Long> findAllRecipesByUser_Id(Long id){
        return this.userActivityRepository.findAllRecipesByUser_Id(id);
    }
    public void saveHistoryViewRecipe(Recipe recipe){
        User user = this.userService.getCurrentUser();
        UserActivity userActivity = user.getUserActivity();

        List<Long> recipeIds = userActivity.getRecipeIds();

        if (recipeIds == null) {
            recipeIds = new ArrayList<>();
        }
        recipeIds.remove(recipe.getId());      // Xóa nếu đã có sẵn id này trong danh sách
        recipeIds.add(0, recipe.getId());      // Thêm vào đầu danh sách (gần đây nhất)
        if (recipeIds.size() > 3) {
            recipeIds = recipeIds.subList(0, 3); // Giữ lại 3 phần tử đầu
        }
        userActivity.setRecipeIds(recipeIds);
        user.setUserActivity(userActivity);
        this.userService.saveUser(user);

    }

}
