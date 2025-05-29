package com.example.ecofood.controller.client;


import com.example.ecofood.domain.Recipe;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PremiumController {

    @GetMapping("/premium")
    public String getPagePremium(Model model, HttpServletRequest request){


        return "client/premium/show";
    }
}
