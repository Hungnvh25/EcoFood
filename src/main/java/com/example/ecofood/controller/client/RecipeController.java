package com.example.ecofood.controller.client;

import com.example.ecofood.domain.Instruction;
import com.example.ecofood.domain.Recipe;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Controller
//@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecipeController {

    @GetMapping("/recipe")
    public String getPageAddRecipe(Model model){
        model.addAttribute("recipe",new Recipe());
        return "/client/Recipe/add";
    }

    @PostMapping("/recipes")
    public String saveRecipe(
            @ModelAttribute("recipe") @Valid Recipe recipe,
            BindingResult result,
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam(value = "instructionImages", required = false) List<MultipartFile> instructionImages,
            @RequestParam(value = "newInstructionImages", required = false) List<MultipartFile> newInstructionImages,
            @RequestParam(value = "newInstructions[].description", required = false) List<String> newInstructionDescriptions,
            @RequestParam(value = "newIngredients[].name", required = false) List<String> newIngredientNames,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            return "/client/Recipe/add";
        }

        // Xử lý ảnh món ăn
//        if (!imageFile.isEmpty()) {
//            try {
//                String fileName = saveFile(imageFile);
//                recipe.setImageUrl("/uploads/" + fileName);
//            } catch (IOException e) {
//                redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu ảnh món ăn");
//                return "redirect:/recipe";
//            }
//        }
//
//        // Xử lý instructions tĩnh (từ th:each)
//        if (instructionImages != null && !instructionImages.isEmpty()) {
//            List<Instruction> instructions = new ArrayList<>(recipe.getInstructions());
//            for (int i = 0; i < instructions.size() && i < instructionImages.size(); i++) {
//                if (!instructionImages.get(i).isEmpty()) {
//                    try {
//                        String fileName = saveFile(instructionImages.get(i));
//                        instructions.get(i).setImageUrl("/uploads/" + fileName);
//                    } catch (IOException e) {
//                        redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu ảnh bước làm");
//                        return "redirect:/recipe";
//                    }
//                }
//            }
//            recipe.setInstructions(new HashSet<>(instructions));
//        }
//
//        // Xử lý instructions động (từ JavaScript)
//        if (newInstructionDescriptions != null && !newInstructionDescriptions.isEmpty()) {
//            Set<Instruction> instructions = recipe.getInstructions();
//            for (int i = 0; i < newInstructionDescriptions.size(); i++) {
//                Instruction instruction = new Instruction();
//                instruction.setDescription(newInstructionDescriptions.get(i));
//                if (newInstructionImages != null && i < newInstructionImages.size() && !newInstructionImages.get(i).isEmpty()) {
//                    try {
//                        String fileName = saveFile(newInstructionImages.get(i));
//                        instruction.setImageUrl("/uploads/" + fileName);
//                    } catch (IOException e) {
//                        redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu ảnh bước làm động");
//                        return "redirect:/recipe";
//                    }
//                }
//                instructions.add(instruction);
//            }
//            recipe.setInstructions(instructions);
//        }
//
//        // Xử lý ingredients động (từ JavaScript)
//        if (newIngredientNames != null && !newIngredientNames.isEmpty()) {
//            Set<Ingredient> ingredients = recipe.getIngredients();
//            for (String name : newIngredientNames) {
//                if (name != null && !name.trim().isEmpty()) {
//                    Ingredient ingredient = new Ingredient();
//                    ingredient.setName(name);
//                    ingredients.add(ingredient);
//                }
//            }
//            recipe.setIngredients(ingredients);
//        }
//
//        // Lưu recipe
//        recipeRepository.save(recipe);

        redirectAttributes.addFlashAttribute("success", "Lưu món ăn thành công");
        return "redirect:/recipes";
    }
}
