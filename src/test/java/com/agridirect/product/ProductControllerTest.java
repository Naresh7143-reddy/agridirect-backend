package com.agridirect.product;

import com.agridirect.product.dto.ProductResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Product Controller Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("Should return products for buyer dashboard successfully without 500 error")
    void testGetAllProductsSuccess() throws Exception {
        ProductResponse response = ProductResponse.builder()
                .id(UUID.randomUUID())
                .name("Fresh Apples")
                .price(150.0)
                .unit("kg")
                .farmerName("John Farmer")
                .categoryName("Fruits")
                .imageUrls(List.of("http://example.com/apple.jpg"))
                .isAvailable(true)
                .build();

        when(productService.getAllProducts()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Fresh Apples"))
                .andExpect(jsonPath("$.data[0].farmerName").value("John Farmer"));
    }

    @Test
    @DisplayName("Should return products by category successfully")
    void testGetProductsByCategorySuccess() throws Exception {
        UUID categoryId = UUID.randomUUID();
        ProductResponse response = ProductResponse.builder()
                .id(UUID.randomUUID())
                .name("Organic Tomatoes")
                .price(40.0)
                .unit("kg")
                .farmerName("Alice Farmer")
                .categoryName("Vegetables")
                .imageUrls(List.of("http://example.com/tomato.jpg"))
                .isAvailable(true)
                .build();

        when(productService.getProductsByCategory(categoryId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/products/category/" + categoryId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Organic Tomatoes"));
    }
}
