package com.agridirect.order;

import com.agridirect.order.dto.ReturnSubmitRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReturnService {

    @Autowired
    private ReturnRequestRepository returnRequestRepository;

    @Autowired
    private OrderRepository orderRepository;

    public ReturnRequest submitReturnRequest(UUID buyerId, ReturnSubmitRequest request) {
        Optional<Order> orderOpt = orderRepository.findById(request.getOrderId());
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Order not found");
        }
        
        Order order = orderOpt.get();
        if (!order.getBuyerId().equals(buyerId)) {
            throw new RuntimeException("Unauthorized");
        }

        order.setReturnStatus("PENDING");
        orderRepository.save(order);

        // Fetch farmerId from order/product mapping (simplified for this example)
        // Assume farmerId is available or we retrieve it from the order items
        UUID mockFarmerId = UUID.randomUUID(); // Placeholder, in real implementation retrieve from order.

        ReturnRequest returnRequest = ReturnRequest.builder()
                .orderId(order.getId())
                .buyerId(buyerId)
                .farmerId(mockFarmerId)
                .reason(request.getReason())
                .status("PENDING")
                .refundAmount(order.getTotalAmount())
                .build();

        return returnRequestRepository.save(returnRequest);
    }

    public List<ReturnRequest> getBuyerReturns(UUID buyerId) {
        return returnRequestRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId);
    }

    public List<ReturnRequest> getFarmerReturns(UUID farmerId) {
        return returnRequestRepository.findByFarmerIdOrderByCreatedAtDesc(farmerId);
    }
}
