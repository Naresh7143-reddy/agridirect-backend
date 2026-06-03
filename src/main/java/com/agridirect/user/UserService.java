package com.agridirect.user;

import com.agridirect.common.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }

    public User findByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }

    public void updateFcmToken(UUID userId, String token) {
        User user = findById(userId);
        user.setFcmToken(token);
        userRepository.save(user);
    }

    public void blockUser(UUID userId) {
        User user = findById(userId);
        user.setActive(false);
        userRepository.save(user);
    }

    public void unblockUser(UUID userId) {
        User user = findById(userId);
        user.setActive(true);
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }
}
