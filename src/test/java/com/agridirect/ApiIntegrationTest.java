package com.agridirect;

import com.agridirect.buyer.BuyerProfile;
import com.agridirect.buyer.BuyerRepository;
import com.agridirect.category.Category;
import com.agridirect.category.CategoryRepository;
import com.agridirect.common.util.JwtUtil;
import com.agridirect.farmer.FarmerProfile;
import com.agridirect.farmer.FarmerRepository;
import com.agridirect.user.User;
import com.agridirect.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full end-to-end API integration tests.
 * Uses H2 in-memory DB via profile "test".
 * Firebase and Redis beans are mocked.
 * JWT auth is exercised with real tokens signed using the test secret.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestConfig.class, TestFirebaseConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JwtUtil jwtUtil;
    @Autowired UserRepository userRepository;
    @Autowired FarmerRepository farmerRepository;
    @Autowired BuyerRepository buyerRepository;
    @Autowired CategoryRepository categoryRepository;

    static String farmerToken, buyerToken, adminToken;
    static UUID farmerId, buyerId, adminId, categoryId, productId, orderId;

    // ── Seed data ────────────────────────────────────────────────────────────

    @BeforeAll
    static void seed(@Autowired UserRepository userRepo,
                     @Autowired FarmerRepository farmerRepo,
                     @Autowired BuyerRepository buyerRepo,
                     @Autowired CategoryRepository catRepo,
                     @Autowired JwtUtil jwt) {

        User farmer = userRepo.save(User.builder()
                .phone("+911111111111").name("Test Farmer")
                .role("FARMER").email("farmer@test.com").isActive(true).build());
        farmerId = farmer.getId();
        farmerRepo.save(FarmerProfile.builder()
                .userId(farmerId).farmName("Green Farm")
                .location("Chennai").verified(true).build());

        User buyer = userRepo.save(User.builder()
                .phone("+912222222222").name("Test Buyer")
                .role("BUYER").email("buyer@test.com").isActive(true).build());
        buyerId = buyer.getId();
        buyerRepo.save(BuyerProfile.builder()
                .userId(buyerId).buyerType("INDIVIDUAL").address("Chennai").build());

        User admin = userRepo.save(User.builder()
                .phone("+919999999999").name("Admin")
                .role("ADMIN").email("admin@test.com").isActive(true).build());
        adminId = admin.getId();

        Category cat = Category.builder().name("Vegetables").isActive(true).build();
        catRepo.save(cat);
        categoryId = cat.getId();

        farmerToken = jwt.generateToken(farmerId.toString(), "FARMER", "+911111111111");
        buyerToken  = jwt.generateToken(buyerId.toString(),  "BUYER",  "+912222222222");
        adminToken  = jwt.generateToken(adminId.toString(),  "ADMIN",  "+919999999999");
    }

    private String auth(String token) { return "Bearer " + token; }

    // ── 1. PUBLIC ENDPOINTS ──────────────────────────────────────────────────

    @Test @Order(1)
    void pub_getCategories() throws Exception {
        mvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
        pass("GET /api/categories");
    }

    @Test @Order(2)
    void pub_getProducts() throws Exception {
        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        pass("GET /api/products");
    }

    @Test @Order(3)
    void pub_searchProducts() throws Exception {
        mvc.perform(get("/api/products/search").param("q", "tomato"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        pass("GET /api/products/search?q=tomato");
    }

    // ── 2. AUTH ──────────────────────────────────────────────────────────────

    @Test @Order(5)
    void auth_getMe_withToken() throws Exception {
        mvc.perform(get("/api/auth/me").header("Authorization", auth(farmerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("FARMER"));
        pass("GET /api/auth/me (JWT)");
    }

    @Test @Order(6)
    void auth_updateFcmToken() throws Exception {
        mvc.perform(put("/api/auth/fcm-token")
                        .header("Authorization", auth(buyerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"fcmToken":"test-fcm-token-abc123"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("FCM token updated"));
        pass("PUT /api/auth/fcm-token");
    }

    // ── 3. FARMER ────────────────────────────────────────────────────────────

    @Test @Order(10)
    void farmer_getProfile() throws Exception {
        mvc.perform(get("/api/farmer/profile").header("Authorization", auth(farmerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.farmName").value("Green Farm"));
        pass("GET /api/farmer/profile");
    }

    @Test @Order(11)
    void farmer_getDashboard() throws Exception {
        mvc.perform(get("/api/farmer/dashboard").header("Authorization", auth(farmerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        pass("GET /api/farmer/dashboard");
    }

    @Test @Order(12)
    void farmer_getEarnings() throws Exception {
        mvc.perform(get("/api/farmer/earnings").header("Authorization", auth(farmerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        pass("GET /api/farmer/earnings");
    }

    @Test @Order(13)
    void farmer_createProduct() throws Exception {
        Map<String, Object> req = Map.of(
                "name", "Fresh Tomatoes",
                "description", "Organic farm tomatoes",
                "price", 40.0,
                "unit", "kg",
                "stockQuantity", 100.0,
                "categoryId", categoryId.toString()
        );
        MvcResult res = mvc.perform(post("/api/farmer/products")
                        .header("Authorization", auth(farmerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Fresh Tomatoes"))
                .andExpect(jsonPath("$.data.price").value(40.0))
                .andReturn();

        productId = UUID.fromString(
                mapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asText());
        pass("POST /api/farmer/products  →  id=" + productId);
    }

    @Test @Order(14)
    void farmer_getMyListings() throws Exception {
        mvc.perform(get("/api/farmer/products").header("Authorization", auth(farmerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
        pass("GET /api/farmer/products");
    }

    @Test @Order(15)
    void farmer_updateProduct() throws Exception {
        Map<String, Object> req = Map.of(
                "name", "Organic Tomatoes",
                "price", 45.0,
                "unit", "kg",
                "stockQuantity", 90.0,
                "categoryId", categoryId.toString()
        );
        mvc.perform(put("/api/farmer/products/" + productId)
                        .header("Authorization", auth(farmerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Organic Tomatoes"))
                .andExpect(jsonPath("$.data.price").value(45.0));
        pass("PUT /api/farmer/products/{id}");
    }

    // ── 4. PUBLIC PRODUCT DETAIL ─────────────────────────────────────────────

    @Test @Order(16)
    void pub_getProductById() throws Exception {
        mvc.perform(get("/api/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(productId.toString()));
        pass("GET /api/products/{id}");
    }

    @Test @Order(17)
    void pub_getProductsByCategory() throws Exception {
        mvc.perform(get("/api/products/category/" + categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        pass("GET /api/products/category/{id}");
    }

    // ── 5. BUYER ─────────────────────────────────────────────────────────────

    @Test @Order(20)
    void buyer_getProfile() throws Exception {
        mvc.perform(get("/api/buyer/profile").header("Authorization", auth(buyerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.buyerType").value("INDIVIDUAL"));
        pass("GET /api/buyer/profile");
    }

    @Test @Order(21)
    void buyer_placeOrder() throws Exception {
        Map<String, Object> req = Map.of(
                "items", List.of(Map.of("productId", productId.toString(), "quantity", 2.0)),
                "deliveryAddress", "123 Test Street, Chennai"
        );
        MvcResult res = mvc.perform(post("/api/buyer/orders")
                        .header("Authorization", auth(buyerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.totalAmount").value(90.0))  // 45 * 2
                .andReturn();

        orderId = UUID.fromString(
                mapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asText());
        pass("POST /api/buyer/orders  →  id=" + orderId + "  total=90.0");
    }

    @Test @Order(22)
    void buyer_getOrders() throws Exception {
        mvc.perform(get("/api/buyer/orders").header("Authorization", auth(buyerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
        pass("GET /api/buyer/orders");
    }

    @Test @Order(23)
    void buyer_getOrderById() throws Exception {
        mvc.perform(get("/api/buyer/orders/" + orderId).header("Authorization", auth(buyerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(orderId.toString()));
        pass("GET /api/buyer/orders/{id}");
    }

    // ── 6. FARMER ORDER FLOW ─────────────────────────────────────────────────

    @Test @Order(30)
    void farmer_getOrders() throws Exception {
        mvc.perform(get("/api/farmer/orders").header("Authorization", auth(farmerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
        pass("GET /api/farmer/orders");
    }

    @Test @Order(31)
    void farmer_acceptOrder() throws Exception {
        mvc.perform(put("/api/farmer/orders/" + orderId + "/accept")
                        .header("Authorization", auth(farmerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
        pass("PUT /api/farmer/orders/{id}/accept");
    }

    @Test @Order(32)
    void farmer_markPacked() throws Exception {
        mvc.perform(put("/api/farmer/orders/" + orderId + "/packed")
                        .header("Authorization", auth(farmerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PACKED"));
        pass("PUT /api/farmer/orders/{id}/packed");
    }

    // ── 7. ADMIN ─────────────────────────────────────────────────────────────

    @Test @Order(40)
    void admin_getAllUsers() throws Exception {
        mvc.perform(get("/api/admin/users").header("Authorization", auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(3))));
        pass("GET /api/admin/users");
    }

    @Test @Order(41)
    void admin_getUsersByRole() throws Exception {
        mvc.perform(get("/api/admin/users/role/FARMER").header("Authorization", auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].role").value("FARMER"));
        pass("GET /api/admin/users/role/FARMER");
    }

    @Test @Order(42)
    void admin_getAllOrders() throws Exception {
        mvc.perform(get("/api/admin/orders").header("Authorization", auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
        pass("GET /api/admin/orders");
    }

    @Test @Order(43)
    void admin_analytics() throws Exception {
        mvc.perform(get("/api/admin/analytics").header("Authorization", auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers",    greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.data.totalFarmers",  greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.totalBuyers",   greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.totalOrders",   greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.totalProducts", greaterThanOrEqualTo(1)));
        pass("GET /api/admin/analytics");
    }

    @Test @Order(44)
    void admin_getPendingFarmers() throws Exception {
        mvc.perform(get("/api/admin/farmers/pending").header("Authorization", auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        pass("GET /api/admin/farmers/pending");
    }

    @Test @Order(45)
    void admin_verifyFarmer() throws Exception {
        mvc.perform(put("/api/admin/farmers/" + farmerId + "/verify")
                        .header("Authorization", auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Farmer verified successfully"));
        pass("PUT /api/admin/farmers/{id}/verify");
    }

    @Test @Order(46)
    void admin_blockUser() throws Exception {
        mvc.perform(put("/api/admin/users/" + buyerId + "/block")
                        .header("Authorization", auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User blocked successfully"));
        pass("PUT /api/admin/users/{id}/block");
    }

    @Test @Order(47)
    void admin_unblockUser() throws Exception {
        mvc.perform(put("/api/admin/users/" + buyerId + "/unblock")
                        .header("Authorization", auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User unblocked successfully"));
        pass("PUT /api/admin/users/{id}/unblock");
    }

    @Test @Order(48)
    void admin_createCategory() throws Exception {
        Map<String, String> req = Map.of("name", "Fruits", "imageUrl", "");
        mvc.perform(post("/api/admin/categories")
                        .header("Authorization", auth(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Fruits"));
        pass("POST /api/admin/categories");
    }

    // ── 8. CANCEL ORDER ──────────────────────────────────────────────────────

    @Test @Order(50)
    void buyer_placeAndCancelOrder() throws Exception {
        // Place fresh PENDING order
        Map<String, Object> req = Map.of(
                "items", List.of(Map.of("productId", productId.toString(), "quantity", 1.0)),
                "deliveryAddress", "Cancel test, Chennai"
        );
        MvcResult res = mvc.perform(post("/api/buyer/orders")
                        .header("Authorization", auth(buyerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        String cid = mapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asText();

        mvc.perform(post("/api/buyer/orders/" + cid + "/cancel")
                        .header("Authorization", auth(buyerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
        pass("POST /api/buyer/orders/{id}/cancel");
    }

    // ── 9. SECURITY ENFORCEMENT ──────────────────────────────────────────────

    @Test @Order(60)
    void security_buyerCannotAccessFarmerEndpoint() throws Exception {
        mvc.perform(get("/api/farmer/profile").header("Authorization", auth(buyerToken)))
                .andExpect(status().is(anyOf(is(401), is(403))));
        pass("SECURITY: BUYER → /api/farmer/profile → 403 ✓");
    }

    @Test @Order(61)
    void security_farmerCannotAccessAdminEndpoint() throws Exception {
        mvc.perform(get("/api/admin/users").header("Authorization", auth(farmerToken)))
                .andExpect(status().is(anyOf(is(401), is(403))));
        pass("SECURITY: FARMER → /api/admin/users → 403 ✓");
    }

    @Test @Order(62)
    void security_invalidTokenRejected() throws Exception {
        mvc.perform(get("/api/farmer/profile").header("Authorization", "Bearer totally.invalid.token"))
                .andExpect(status().is(anyOf(is(401), is(403))));
        pass("SECURITY: invalid JWT → 403 ✓");
    }

    @Test @Order(63)
    void security_noTokenRejected() throws Exception {
        mvc.perform(get("/api/farmer/profile"))
                .andExpect(status().is(anyOf(is(401), is(403))));
        pass("SECURITY: no token → 403 ✓");
    }

    // ── 10. CLEANUP ──────────────────────────────────────────────────────────

    @Test @Order(70)
    void farmer_deleteProduct() throws Exception {
        mvc.perform(delete("/api/farmer/products/" + productId)
                        .header("Authorization", auth(farmerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Product removed"));
        pass("DELETE /api/farmer/products/{id}");
    }

    // ── helper ───────────────────────────────────────────────────────────────
    static void pass(String label) {
        System.out.println("  ✅  " + label);
    }
}
