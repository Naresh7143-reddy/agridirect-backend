package com.agridirect.category;

import com.agridirect.common.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class CategoryController {

    @Autowired private CategoryService categoryService;

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategories()));
    }

    @PostMapping("/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Category>> createCategory(@RequestBody Map<String, String> body) {
        Category created = categoryService.createCategory(body.get("name"), body.get("imageUrl"));
        return ResponseEntity.ok(ApiResponse.success("Category created", created));
    }

    @PutMapping("/admin/categories/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Category>> toggleCategory(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.toggleCategory(id)));
    }
}
