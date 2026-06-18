package com.agridirect.security;

import com.agridirect.common.dto.ApiResponse;
import com.agridirect.common.exception.ApiException;
import com.agridirect.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GlobalExceptionHandler — verifies that each exception type
 * is mapped to the correct HTTP status and that error messages never leak
 * sensitive internal information.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("ApiException is mapped to its own status code")
    void apiException_correctStatus() {
        ApiException ex = new ApiException("User not found", HttpStatus.NOT_FOUND);
        ResponseEntity<ApiResponse<Void>> resp = handler.handleApiException(ex);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("User not found");
    }

    @Test
    @DisplayName("[SECURITY] AccessDeniedException returns 403 — not 401 or 500")
    void accessDenied_returns403() {
        AccessDeniedException ex = new AccessDeniedException("forbidden");
        ResponseEntity<ApiResponse<Void>> resp = handler.handleAccessDenied(ex);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).doesNotContain("Exception");
    }

    @Test
    @DisplayName("[SECURITY] Wrong HTTP method returns 405 — not 500")
    void wrongMethod_returns405() {
        HttpRequestMethodNotSupportedException ex =
                new HttpRequestMethodNotSupportedException("DELETE");
        ResponseEntity<ApiResponse<Void>> resp = handler.handleMethodNotAllowed(ex);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    @DisplayName("[SECURITY] Malformed JSON body returns 400 — not 500")
    void malformedJson_returns400() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        ResponseEntity<ApiResponse<Void>> resp = handler.handleUnreadable(ex);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).doesNotContain("Exception");
    }

    @Test
    @DisplayName("[SECURITY] Generic Exception returns 500 with safe generic message")
    void genericException_returns500_safeMessage() {
        RuntimeException ex = new RuntimeException("DB credentials: password=secret123");
        ResponseEntity<ApiResponse<Void>> resp = handler.handleException(ex);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        // Should NOT leak the raw exception message
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).doesNotContain("password=secret123");
    }

    @Test
    @DisplayName("[SECURITY] DataAccessException returns 500 with safe message — no SQL leak")
    void dataAccessException_noSqlLeak() {
        org.springframework.dao.DataAccessException ex =
                new org.springframework.dao.DataIntegrityViolationException(
                        "insert into users (password_hash) values ('md5:secret')");
        ResponseEntity<ApiResponse<Void>> resp = handler.handleDataAccess(ex);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).doesNotContain("md5:secret");
        assertThat(resp.getBody().getMessage()).doesNotContain("insert into");
    }

    @Test
    @DisplayName("ApiResponse.error() sets success=false")
    void apiResponse_error_successFalse() {
        ApiResponse<Void> resp = ApiResponse.error("something went wrong");
        assertThat(resp.isSuccess()).isFalse();
        assertThat(resp.getMessage()).isEqualTo("something went wrong");
    }

    @Test
    @DisplayName("ApiResponse.success() sets success=true")
    void apiResponse_success_successTrue() {
        ApiResponse<String> resp = ApiResponse.success("created");
        assertThat(resp.isSuccess()).isTrue();
    }
}
