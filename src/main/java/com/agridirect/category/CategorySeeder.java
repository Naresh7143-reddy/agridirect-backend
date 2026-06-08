package com.agridirect.category;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds a default set of product categories on startup if the categories table
 * is empty. Without any categories, farmers cannot select one in "Add Product"
 * (the form requires a category), which made the feature look broken
 * ("product not adding").
 */
@Component
public class CategorySeeder implements CommandLineRunner {

    @Autowired private CategoryRepository categoryRepository;

    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "Vegetables",
            "Fruits",
            "Grains & Cereals",
            "Pulses & Lentils",
            "Spices & Herbs",
            "Dairy Products",
            "Flowers",
            "Other"
    );

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0) return;
        for (String name : DEFAULT_CATEGORIES) {
            categoryRepository.save(Category.builder().name(name).isActive(true).build());
        }
    }
}
