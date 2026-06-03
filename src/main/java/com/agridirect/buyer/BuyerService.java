package com.agridirect.buyer;

import com.agridirect.common.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class BuyerService {

    @Autowired private BuyerRepository buyerRepository;

    public BuyerProfile getProfile(UUID userId) {
        return buyerRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException("Buyer profile not found", HttpStatus.NOT_FOUND));
    }

    public BuyerProfile updateProfile(UUID userId, Map<String, Object> updates) {
        BuyerProfile profile = getProfile(userId);
        if (updates.get("buyerType") != null)  profile.setBuyerType((String) updates.get("buyerType"));
        if (updates.get("address") != null)    profile.setAddress((String) updates.get("address"));
        if (updates.get("gstNumber") != null)  profile.setGstNumber((String) updates.get("gstNumber"));
        return buyerRepository.save(profile);
    }
}
