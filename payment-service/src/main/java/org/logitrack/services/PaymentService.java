package org.logitrack.services;

import org.logitrack.dto.request.PaymentRequest;
import org.logitrack.dto.response.ApiResponse;
import org.springframework.stereotype.Component;

@Component
public interface PaymentService {
    ApiResponse initializePayment(PaymentRequest request, String email);

    ApiResponse verifyPayment(String request, String email);
}
