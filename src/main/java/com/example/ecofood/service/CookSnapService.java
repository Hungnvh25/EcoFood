package com.example.ecofood.service;

import com.example.ecofood.domain.CookSnap;
import com.example.ecofood.domain.Recipe;
import com.example.ecofood.domain.User;
import com.example.ecofood.repository.CookSnapRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

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

}
