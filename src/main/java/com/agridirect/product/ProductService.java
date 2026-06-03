package com.agridirect.product;

import com.agridirect.category.Category;
import com.agridirect.category.CategoryRepository;
import com.agridirect.common.exception.ApiException;
import com.agridirect.farmer.FarmerProfile;
import com.agridirect.farmer.FarmerRepository;
import com.agridirect.product.dto.ProductRequest;
import com.agridirect.product.dto.ProductResponse;
import com.agridirect.user.User;
import com.agridirect.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class ProductService {

    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private FarmerRepository farmerRepository;
    @Autowired private CategoryRepository categoryRepository;

    private ProductResponse buildResponse(Product p) {
        String farmerName = userRepository.findById(p.getFarmerId())
                .map(User::getName).orElse("Unknown Farmer");

        String farmerLocation = farmerRepository.findByUserId(p.getFarmerId())
                .map(FarmerProfile::getLocation).orElse(null);

        String categoryName = p.getCategoryId() != null
                ? categoryRepository.findById(p.getCategoryId()).map(Category::getName).orElse(null)
                : null;

        return ProductResponse.builder()
                .id(p.getId())
                .farmerId(p.getFarmerId())
                .categoryId(p.getCategoryId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .unit(p.getUnit())
                .stockQuantity(p.getStockQuantity())
                .imageUrls(p.getImageUrls())
                .isAvailable(p.isAvailable())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .farmerName(farmerName)
                .farmerLocation(farmerLocation)
                .categoryName(categoryName)
                .averageRating(0.0)
                .build();
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findByIsAvailableTrue().stream()
                .map(this::buildResponse).collect(Collectors.toList());
    }

    public ProductResponse getProductById(UUID id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ApiException("Product not found", HttpStatus.NOT_FOUND));
        return buildResponse(p);
    }

    public List<ProductResponse> getProductsByCategory(UUID categoryId) {
        return productRepository.findByCategoryIdAndIsAvailableTrue(categoryId).stream()
                .map(this::buildResponse).collect(Collectors.toList());
    }

    public List<ProductResponse> searchProducts(String query) {
        return productRepository.findByNameContainingIgnoreCaseAndIsAvailableTrue(query).stream()
                .map(this::buildResponse).collect(Collectors.toList());
    }

    public List<ProductResponse> getMyListings(UUID farmerId) {
        return productRepository.findByFarmerId(farmerId).stream()
                .map(this::buildResponse).collect(Collectors.toList());
    }

    public ProductResponse createProduct(UUID farmerId, ProductRequest req) {
        if (req.getCategoryId() != null && !categoryRepository.existsById(req.getCategoryId())) {
            throw new ApiException("Category not found", HttpStatus.BAD_REQUEST);
        }
        Product product = Product.builder()
                .farmerId(farmerId)
                .categoryId(req.getCategoryId())
                .name(req.getName())
                .description(req.getDescription())
                .price(req.getPrice())
                .unit(req.getUnit())
                .stockQuantity(req.getStockQuantity())
                .imageUrls(req.getImageUrls())
                .isAvailable(true)
                .build();
        return buildResponse(productRepository.save(product));
    }

    public ProductResponse updateProduct(UUID farmerId, UUID productId, ProductRequest req) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException("Product not found", HttpStatus.NOT_FOUND));
        if (!product.getFarmerId().equals(farmerId)) {
            throw new ApiException("Not your product", HttpStatus.FORBIDDEN);
        }
        if (req.getCategoryId() != null && !categoryRepository.existsById(req.getCategoryId())) {
            throw new ApiException("Category not found", HttpStatus.BAD_REQUEST);
        }
        if (req.getName() != null)          product.setName(req.getName());
        if (req.getDescription() != null)   product.setDescription(req.getDescription());
        if (req.getPrice() != null)         product.setPrice(req.getPrice());
        if (req.getUnit() != null)          product.setUnit(req.getUnit());
        if (req.getStockQuantity() != null) product.setStockQuantity(req.getStockQuantity());
        if (req.getCategoryId() != null)    product.setCategoryId(req.getCategoryId());
        if (req.getImageUrls() != null)     product.setImageUrls(req.getImageUrls());
        return buildResponse(productRepository.save(product));
    }

    public void deleteProduct(UUID farmerId, UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException("Product not found", HttpStatus.NOT_FOUND));
        if (!product.getFarmerId().equals(farmerId)) {
            throw new ApiException("Not your product", HttpStatus.FORBIDDEN);
        }
        product.setAvailable(false);
        productRepository.save(product);
    }

    public void updateStock(UUID productId, Double quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException("Product not found", HttpStatus.NOT_FOUND));
        double remaining = (product.getStockQuantity() != null ? product.getStockQuantity() : 0.0) - quantity;
        if (remaining <= 0) {
            product.setStockQuantity(0.0);
            product.setAvailable(false);
        } else {
            product.setStockQuantity(remaining);
        }
        productRepository.save(product);
    }
}
