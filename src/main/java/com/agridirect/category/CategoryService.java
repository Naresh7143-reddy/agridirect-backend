package com.agridirect.category;

import com.agridirect.common.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {

    @Autowired private CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findByIsActiveTrue();
    }

    public Category createCategory(String name, String imageUrl) {
        if (categoryRepository.findByName(name).isPresent()) {
            throw new ApiException("Category '" + name + "' already exists", HttpStatus.CONFLICT);
        }
        return categoryRepository.save(Category.builder()
                .name(name)
                .imageUrl(imageUrl)
                .isActive(true)
                .build());
    }

    public Category toggleCategory(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException("Category not found", HttpStatus.NOT_FOUND));
        category.setActive(!category.isActive());
        return categoryRepository.save(category);
    }
}
