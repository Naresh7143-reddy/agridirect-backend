package com.agridirect.product;

import com.agridirect.common.dto.ApiResponse;
import com.agridirect.product.dto.ProductRequest;
import com.agridirect.product.dto.ProductResponse;
import com.agridirect.storage.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
public class ProductController {

    @Autowired private ProductService productService;
    @Autowired private CloudinaryService cloudinaryService;

    @GetMapping("/api/products")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        return ResponseEntity.ok(ApiResponse.success(productService.getAllProducts()));
    }

    @GetMapping("/api/products/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id)));
    }

    @GetMapping("/api/products/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getByCategory(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductsByCategory(categoryId)));
    }

    @GetMapping("/api/products/search")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> search(@RequestParam("q") String query) {
        return ResponseEntity.ok(ApiResponse.success(productService.searchProducts(query)));
    }

    @GetMapping("/api/farmer/products")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getMyListings() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(productService.getMyListings(UUID.fromString(userId))));
    }

    @PostMapping("/api/farmer/products")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@RequestBody ProductRequest req) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success("Product created", productService.createProduct(UUID.fromString(userId), req)));
    }

    @PutMapping("/api/farmer/products/{id}")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@PathVariable UUID id,
                                                                       @RequestBody ProductRequest req) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(productService.updateProduct(UUID.fromString(userId), id, req)));
    }

    @DeleteMapping("/api/farmer/products/{id}")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable UUID id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        productService.deleteProduct(UUID.fromString(userId), id);
        return ResponseEntity.ok(ApiResponse.success("Product removed", null));
    }

    @PostMapping("/api/farmer/products/upload-image")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<String>> uploadImage(@RequestParam MultipartFile file) throws Exception {
        String url = cloudinaryService.uploadProductImage(file);
        return ResponseEntity.ok(ApiResponse.success("Image uploaded", url));
    }
}
