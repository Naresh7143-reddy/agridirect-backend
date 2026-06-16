package com.agridirect.api.test;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;

import static io.restassured.RestAssured.given;

/**
 * Base class for API testing with REST Assured
 */
public class BaseAPITest {
    
    protected static final String BASE_URL = "http://localhost:8001/api";
    protected static final String AUTH_ENDPOINT = BASE_URL + "/auth";
    protected static final String FARMER_ENDPOINT = BASE_URL + "/farmer";
    protected static final String BUYER_ENDPOINT = BASE_URL + "/buyer";
    protected static final String PRODUCT_ENDPOINT = BASE_URL + "/products";
    protected static final String ORDER_ENDPOINT = BASE_URL + "/orders";
    protected static final String PAYMENT_ENDPOINT = BASE_URL + "/payment";
    protected static final String CATEGORY_ENDPOINT = BASE_URL + "/categories";
    protected static final String ADMIN_ENDPOINT = BASE_URL + "/admin";
    protected static final String DELIVERY_ENDPOINT = BASE_URL + "/delivery";
    protected static final String AI_ENDPOINT = BASE_URL + "/ai";
    
    protected String authToken = null;
    
    @BeforeClass
    public void setup() {
        RestAssured.baseURI = BASE_URL;
    }
    
    /**
     * Helper method to make GET requests
     */
    protected Response getRequest(String endpoint) {
        return given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get(endpoint);
    }
    
    /**
     * Helper method to make POST requests
     */
    protected Response postRequest(String endpoint, Object body) {
        return given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .body(body)
                .when()
                .post(endpoint);
    }
    
    /**
     * Helper method to make PUT requests
     */
    protected Response putRequest(String endpoint, Object body) {
        return given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .body(body)
                .when()
                .put(endpoint);
    }
    
    /**
     * Helper method to make DELETE requests
     */
    protected Response deleteRequest(String endpoint) {
        return given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .when()
                .delete(endpoint);
    }
    
    /**
     * Helper method to make public GET requests (without authentication)
     */
    protected Response publicGetRequest(String endpoint) {
        return given()
                .header("Content-Type", "application/json")
                .when()
                .get(endpoint);
    }
    
    /**
     * Helper method to make public POST requests (without authentication)
     */
    protected Response publicPostRequest(String endpoint, Object body) {
        return given()
                .header("Content-Type", "application/json")
                .body(body)
                .when()
                .post(endpoint);
    }
}
