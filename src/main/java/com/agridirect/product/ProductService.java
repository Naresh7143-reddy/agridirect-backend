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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private FarmerRepository farmerRepository;
    @Autowired private CategoryRepository categoryRepository;

    /** Single-product build — used when only one product needs resolution. */
    private ProductResponse buildResponse(Product p) {
        String farmerName = userRepository.findById(p.getFarmerId())
                .map(User::getName).orElse("Unknown Farmer");
        String farmerLocation = farmerRepository.findByUserId(p.getFarmerId())
                .map(FarmerProfile::getLocation).orElse(null);
        String categoryName = p.getCategoryId() != null
                ? categoryRepository.findById(p.getCategoryId()).map(Category::getName).orElse(null)
                : null;
        return toResponse(p, farmerName, farmerLocation, categoryName);
    }

    /**
     * Batch build — resolves all related entities in 3 queries total (not N*3).
     * Eliminates the N+1 problem that was causing 2163ms avg on GET /api/products.
     */
    private List<ProductResponse> buildResponsesBatch(List<Product> products) {
        if (products.isEmpty()) return List.of();

        // Collect unique IDs
        Set<UUID> farmerIds   = products.stream().map(Product::getFarmerId).collect(Collectors.toSet());
        Set<UUID> categoryIds = products.stream()
                .filter(p -> p.getCategoryId() != null)
                .map(Product::getCategoryId).collect(Collectors.toSet());

        // 1 query: all users for these farmer IDs
        Map<UUID, String> farmerNames = userRepository.findAllById(farmerIds).stream()
                .collect(Collectors.toMap(User::getId, User::getName));

        // 1 query: all farmer profiles for these farmer IDs
        Map<UUID, String> farmerLocations = farmerRepository.findAllByUserIdIn(farmerIds).stream()
                .collect(Collectors.toMap(FarmerProfile::getUserId, fp -> fp.getLocation() != null ? fp.getLocation() : ""));

        // 1 query: all categories needed
        Map<UUID, String> categoryNames = categoryIds.isEmpty() ? Map.of() :
                categoryRepository.findAllById(categoryIds).stream()
                        .collect(Collectors.toMap(Category::getId, Category::getName));

        return products.stream().map(p -> toResponse(
                p,
                farmerNames.getOrDefault(p.getFarmerId(), "Unknown Farmer"),
                farmerLocations.get(p.getFarmerId()),
                p.getCategoryId() != null ? categoryNames.get(p.getCategoryId()) : null
        )).collect(Collectors.toList());
    }

    private ProductResponse toResponse(Product p, String farmerName, String farmerLocation, String categoryName) {
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
        return buildResponsesBatch(productRepository.findByIsAvailableTrue());
    }

    public ProductResponse getProductById(UUID id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ApiException("Product not found", HttpStatus.NOT_FOUND));
        return buildResponse(p);
    }

    public List<ProductResponse> getProductsByCategory(UUID categoryId) {
        return buildResponsesBatch(productRepository.findByCategoryIdAndIsAvailableTrue(categoryId));
    }

    public List<ProductResponse> searchProducts(String query) {
        // Strip null bytes and control chars — prevents PostgreSQL UTF8 encoding errors
        String safe = query == null ? "" : query.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "").trim();
        return buildResponsesBatch(productRepository.findByNameContainingIgnoreCaseAndIsAvailableTrue(safe));
    }

    public List<ProductResponse> getMyListings(UUID farmerId) {
        return buildResponsesBatch(productRepository.findByFarmerId(farmerId));
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
