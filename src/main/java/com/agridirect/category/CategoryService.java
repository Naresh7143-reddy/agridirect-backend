package com.agridirect.category;

import com.agridirect.common.exception.ApiException;
import com.agridirect.product.ProductService;
import com.agridirect.product.dto.ProductResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductService productService;

    public List<Category> getAllCategories() {
        return categoryRepository.findByIsActiveTrue();
    }

    public Category getCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException("Category not found", HttpStatus.NOT_FOUND));
    }

    public List<ProductResponse> getCategoryProducts(UUID id) {
        getCategoryById(id);
        return productService.getProductsByCategory(id);
    }

    public Category updateCategory(UUID id, String name, String imageUrl, Boolean isActive) {
        Category category = getCategoryById(id);
        if (name != null) category.setName(name);
        if (imageUrl != null) category.setImageUrl(imageUrl);
        if (isActive != null) category.setActive(isActive);
        return categoryRepository.save(category);
    }

    public Category updateCategoryImage(UUID id, String imageUrl) {
        Category category = getCategoryById(id);
        category.setImageUrl(imageUrl);
        return categoryRepository.save(category);
    }

    public void deleteCategory(UUID id) {
        Category category = getCategoryById(id);
        categoryRepository.delete(category);
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
