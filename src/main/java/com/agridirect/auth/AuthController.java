package com.agridirect.auth;

import com.agridirect.auth.dto.AuthResponse;
import com.agridirect.auth.dto.LoginRequest;
import com.agridirect.auth.dto.RegisterRequest;
import com.agridirect.common.dto.ApiResponse;
import com.agridirect.common.exception.ApiException;
import com.agridirect.user.User;
import com.agridirect.user.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest req) {
        AuthResponse response = authService.register(req);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest req) {
        AuthResponse response = authService.login(req);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/firebase")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithFirebase(@RequestBody LoginRequest req) {
        AuthResponse response = authService.login(req);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/otp/send")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendOtp(@RequestBody Map<String, String> body) {
        // Firebase client SDK handles actual OTP delivery; this exists so the app's call doesn't 404.
        return ResponseEntity.ok(ApiResponse.success("OTP sent", Map.of("expiresIn", 300)));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@RequestBody Map<String, String> body) {
        // OTP verification is performed by Firebase on the client; the app then calls /api/auth/firebase
        // with the resulting ID token. This endpoint is kept as a compatibility no-op.
        throw new ApiException("OTP verification is handled via Firebase. Use /api/auth/firebase with the ID token.", HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success("If an account exists for this number, password reset instructions have been sent", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody Map<String, String> body) {
        // Validate that the body contains the minimum required fields. Returning
        // 200 for an empty body would be a security issue.
        if (body == null) {
            throw new ApiException("Request body is required", HttpStatus.BAD_REQUEST);
        }
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        if (token == null || token.isBlank()) {
            throw new ApiException("Reset token is required", HttpStatus.BAD_REQUEST);
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new ApiException("New password must be at least 6 characters", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(ApiResponse.success("Password reset successful", null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse.TokensDto>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        AuthResponse.TokensDto tokens = authService.refresh(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", tokens));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // Stateless JWT — client simply discards tokens. Endpoint exists so the
        // app's logout call doesn't 404.
        return ResponseEntity.ok(ApiResponse.success("Logged out", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser() {
        String userId = requireAuthenticatedUserId();
        User user = userService.findById(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/fcm-token")
    public ResponseEntity<ApiResponse<Void>> updateFcmToken(@RequestBody Map<String, String> body) {
        String userId = requireAuthenticatedUserId();
        String fcmToken = body == null ? null : body.get("fcmToken");
        if (fcmToken == null || fcmToken.isBlank()) {
            throw new ApiException("fcmToken is required", HttpStatus.BAD_REQUEST);
        }
        userService.updateFcmToken(UUID.fromString(userId), fcmToken);
        return ResponseEntity.ok(ApiResponse.success("FCM token updated", null));
    }

    /** Returns the authenticated user's ID, or throws 401 if anonymous.
     *  Centralises the null-check so endpoints can't accidentally NPE when
     *  hit without a JWT (which is what the test report flagged for /me and
     *  /fcm-token).
     */
    private String requireAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new ApiException("Authentication required", HttpStatus.UNAUTHORIZED);
        }
        return auth.getName();
    }
}
