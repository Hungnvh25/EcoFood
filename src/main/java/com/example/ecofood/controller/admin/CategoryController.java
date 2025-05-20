package com.example.ecofood.controller.admin;

import com.example.ecofood.domain.Category;
import com.example.ecofood.service.CategoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/category")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // Hiển thị danh sách category với tìm kiếm và phân trang
    @GetMapping
    public String listCategories(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        Pageable pageable = PageRequest.of(page, 10); // 10 items per page
        Page<Category> categories;

        if (name != null && !name.isEmpty()) {
            categories = categoryService.searchByName(name, pageable);
        } else {
            categories = categoryService.findAll(pageable);
        }

        model.addAttribute("categories", categories);
        model.addAttribute("totalCategories", categoryService.count());
        model.addAttribute("param", new java.util.HashMap<String, String>() {{
            put("name", name);
        }});

        return "admin/category/show";
    }

    // Xử lý thêm category từ modal
    @PostMapping("/add")
    public String addCategory(
            @RequestParam("name") String name,
            RedirectAttributes redirectAttributes) {
        try {
            Category category = new Category();
            category.setName(name);
            categoryService.save(category);
            redirectAttributes.addFlashAttribute("success", "Category added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add category: " + e.getMessage());
        }
        return "redirect:/admin/category";
    }

    // Xử lý sửa category từ modal
    @PostMapping("/update")
    public String updateCategory(
            @RequestParam("id") Long id,
            @RequestParam("name") String name,
            RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryService.findById(id);
            if (category == null) {
                throw new IllegalArgumentException("Category not found!");
            }
            category.setName(name);
            categoryService.update(category);
            redirectAttributes.addFlashAttribute("success", "Category updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update category: " + e.getMessage());
        }
        return "redirect:/admin/category";
    }

    // Xử lý xóa category
    @PostMapping("/delete")
    public String deleteCategory(
            @RequestParam("id") Long id,
            RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Category deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete category: " + e.getMessage());
        }
        return "redirect:/admin/category";
    }
}