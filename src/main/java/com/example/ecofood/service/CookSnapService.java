package com.example.ecofood.service;

import com.example.ecofood.domain.CookSnap;
import com.example.ecofood.domain.Recipe;
import com.example.ecofood.domain.User;
import com.example.ecofood.repository.CookSnapRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CookSnapService {
    CookSnapRepository cookSnapRepository;
    UserService userService;
    public Recipe saveCookSnap(CookSnap cookSnap,Recipe recipe){
        User user = this.userService.getCurrentUser();

        cookSnap.setUser(user);
        cookSnap.setRecipe(recipe);

        this.cookSnapRepository.save(cookSnap);
        return recipe;
    }


    public List<CookSnap> getCookSnapByUser(User user){
      return this.cookSnapRepository.findCookSnapByUserId(user.getId());
    }

    public List<CookSnap> getCookSnapByUserFilters(List<Recipe> recipes){
        User user = this.userService.getCurrentUser();

        List<CookSnap> cookSnaps = this.cookSnapRepository.findCookSnapByUserId(user.getId());

        Set<Long> recipeIds = new HashSet<>();
        for (Recipe recipe : recipes) {
            recipeIds.add(recipe.getId());
        }

        cookSnaps.removeIf(cookSnap -> !recipeIds.contains(cookSnap.getRecipe().getId()));

        return cookSnaps;
    }

}
