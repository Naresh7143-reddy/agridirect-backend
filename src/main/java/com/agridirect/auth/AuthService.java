package com.agridirect.auth;

import com.agridirect.auth.dto.AuthResponse;
import com.agridirect.auth.dto.LoginRequest;
import com.agridirect.auth.dto.RegisterRequest;
import com.agridirect.buyer.BuyerProfile;
import com.agridirect.buyer.BuyerRepository;
import com.agridirect.common.exception.ApiException;
import com.agridirect.common.util.JwtUtil;
import com.agridirect.delivery.DeliveryProfile;
import com.agridirect.delivery.DeliveryRepository;
import com.agridirect.farmer.FarmerProfile;
import com.agridirect.farmer.FarmerRepository;
import com.agridirect.user.User;
import com.agridirect.user.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FarmerRepository farmerRepository;

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest req) {
        try {
            FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(req.getIdToken());
            String phone = decoded.getClaims().get("phone_number").toString();

            if (userRepository.existsByPhone(phone)) {
                throw new ApiException("Phone already registered. Please login.", HttpStatus.CONFLICT);
            }

            User user = User.builder()
                    .phone(phone)
                    .name(req.getName())
                    .role(req.getRole())
                    .email(req.getEmail())
                    .isActive(true)
                    .build();
            user = userRepository.save(user);

            switch (req.getRole().toUpperCase()) {
                case "FARMER" -> farmerRepository.save(FarmerProfile.builder()
                        .userId(user.getId())
                        .farmName(req.getFarmName())
                        .location(req.getLocation())
                        .landAcres(req.getLandAcres())
                        .verified(false)
                        .build());

                case "BUYER" -> buyerRepository.save(BuyerProfile.builder()
                        .userId(user.getId())
                        .buyerType(req.getBuyerType())
                        .address(req.getAddress())
                        .gstNumber(req.getGstNumber())
                        .build());

                case "DELIVERY" -> deliveryRepository.save(DeliveryProfile.builder()
                        .userId(user.getId())
                        .vehicleType(req.getVehicleType())
                        .licenseNo(req.getLicenseNo())
                        .isAvailable(true)
                        .build());
            }

            String token = jwtUtil.generateToken(user.getId().toString(), user.getRole(), phone);
            return new AuthResponse(token, user.getRole(), user.getName(), user.getId().toString(), phone);

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Registration failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public AuthResponse login(LoginRequest req) {
        try {
            FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(req.getIdToken());
            String phone = decoded.getClaims().get("phone_number").toString();

            User user = userRepository.findByPhone(phone)
                    .orElseThrow(() -> new ApiException("User not found. Please register first.", HttpStatus.NOT_FOUND));

            if (!user.isActive()) {
                throw new ApiException("Account has been blocked. Contact support.", HttpStatus.FORBIDDEN);
            }

            if (req.getFcmToken() != null) {
                user.setFcmToken(req.getFcmToken());
                userRepository.save(user);
            }

            String token = jwtUtil.generateToken(user.getId().toString(), user.getRole(), phone);
            return new AuthResponse(token, user.getRole(), user.getName(), user.getId().toString(), phone);

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Login failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Optional<User> getCurrentUser(String userId) {
        return userRepository.findById(java.util.UUID.fromString(userId));
    }
}
