package com.example.ecofood.service;

import com.example.ecofood.domain.Category;
import com.example.ecofood.repository.CategoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryService {

    private CategoryRepository categoryRepository;

    public Page<Category> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    public Page<Category> searchByName(String name, Pageable pageable) {
        return categoryRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    public long count() {
        return categoryRepository.count();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public void save(Category category) {
        categoryRepository.save(category);
    }

    public void update(Category category) {
        if (category.getId() == null || !categoryRepository.existsById(category.getId())) {
            throw new IllegalArgumentException("Category does not exist!");
        }
        categoryRepository.save(category);
    }

    public void deleteById(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new IllegalArgumentException("Category does not exist!");
        }
        categoryRepository.deleteById(id);
    }
}