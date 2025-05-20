package com.example.ecofood.Util;

import com.example.ecofood.domain.Recipe;
import com.example.ecofood.domain.User;
import com.example.ecofood.domain.UserActivity;
import com.example.ecofood.service.RecipeService;
import com.example.ecofood.service.UserActivityService;
import com.example.ecofood.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalModelAttribute {

    @Autowired
    private UserService userService;

    @Autowired
    private UserActivityService userActivityService;

    @Autowired
    private RecipeService recipeService;

    @ModelAttribute
    public void addAttributes(Model model, HttpSession session) {
//        Lấy user đăng nhập
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            user = this.userService.getCurrentUser();
            if (user != null) {
                session.setAttribute("currentUser", user);
            }
        }

//        lấy danh sách đã xem món ăn của user
        List<Recipe> recipeList = new ArrayList<>();

        if (user != null) {
            List<Long> recipeIdView = this.userActivityService.findAllRecipesByUser_Id(user.getId());
            for (Long id : recipeIdView) {
                Recipe recipe = this.recipeService.getRecipeById(id);
                recipeList.add(recipe);
            }
        }
        model.addAttribute("currentUser", user);
        model.addAttribute("RecipeHistory", recipeList);

    }

}
