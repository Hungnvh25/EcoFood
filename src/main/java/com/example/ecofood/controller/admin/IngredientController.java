package com.example.ecofood.controller.admin;

import com.example.ecofood.domain.Ingredient;
import com.example.ecofood.service.ImageService;
import com.example.ecofood.service.IngredientService;
import com.example.ecofood.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IngredientController {


    IngredientService ingredientService;
    UserService userService;
    ImageService imageService;

    @GetMapping("/ingredient")
    public String getIngredientDashboard(Model model,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(required = false) String name,
                                         @RequestParam(required = false) String description) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Ingredient> ingredients;

        if (name != null && !name.isEmpty() || description != null && !description.isEmpty()) {
            ingredients = this.ingredientService.searchIngredients(name, description, pageable);
        } else {
            ingredients = this.ingredientService.getAllIngredients(pageable);
        }

        model.addAttribute("ingredients", ingredients);
        model.addAttribute("newIngredient", new Ingredient());
        model.addAttribute("totalUsers", this.userService.getTotalUsers());
        model.addAttribute("revenue", "$12,345");
        model.addAttribute("totalIngredients", this.ingredientService.getTotalIngredients());

        if (!model.containsAttribute("editIngredient")) {
            model.addAttribute("editIngredient", new Ingredient());
        }
        return "admin/ingredient/show";
    }


    @PostMapping("/ingredient/add")
    public String addIngredient(
            @Valid @ModelAttribute("newIngredient") Ingredient ingredient,
            BindingResult result,
            @RequestParam("imageFile") MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("false", "Thêm nguyên thất bại");
            return "redirect:/admin/ingredient";
        }

        // Lưu ingredient vào database
        this.ingredientService.saveIngredient(ingredient);
        redirectAttributes.addFlashAttribute("success", "Thêm nguyên liệu thành công");
        return "redirect:/admin/ingredient";
    }

    @PostMapping("/ingredient/update")
    public String updateIngredient(@Valid @ModelAttribute("editIngredient") Ingredient ingredient, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            result.getAllErrors().forEach(error -> {
                errorMessage.append(error.getDefaultMessage()).append("; ");
            });
            redirectAttributes.addFlashAttribute("false", errorMessage.toString());
            return "redirect:/admin/ingredient";
        }


        this.ingredientService.updateIngredient(ingredient);
        redirectAttributes.addFlashAttribute("success", "Cập nhật nguyên liệu thành công");
        return "redirect:/admin/ingredient";
    }

    @PostMapping("/ingredient/delete")
    public String deleteIngredient(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {


        try {
            this.ingredientService.deleteIngredient(id);
            redirectAttributes.addFlashAttribute("success", "Xóa nguyên liệu thành công");
            return "redirect:/admin/ingredient";
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("false", "Xóa nguyên thất bại "+ e.getMessage());
            return "redirect:/admin/ingredient";
        }
    }
}