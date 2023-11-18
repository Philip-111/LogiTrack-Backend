package org.logitrack.controller;

import org.logitrack.securities.JWTService;
import org.logitrack.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.logitrack.dto.request.PaymentRequest;
import org.logitrack.dto.response.ApiResponse;
import org.logitrack.exceptions.CommonApplicationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment-gateway")
public class PaymentController {

    private final PaymentService paymentService;
    private final JWTService jwtService;

    @PostMapping(path = "/initialize")
    public ResponseEntity<ApiResponse> initiatePayment(@RequestBody PaymentRequest request, @RequestHeader("Authorization") String authorizationHeader) throws CommonApplicationException {
        var userDetails= jwtService.validateTokenAndReturnDetail(authorizationHeader.substring(7));
        log.info("Verify payment ", userDetails.get(" name"));
        return ResponseEntity.ok(paymentService.initializePayment(request, userDetails.get("email")));
    }

    @GetMapping(path = "/verify")
    public ResponseEntity<ApiResponse> verifyPayment(@RequestParam String request, @RequestHeader("Authorization") String authorizationHeader) throws CommonApplicationException {
        var userDetails= jwtService.validateTokenAndReturnDetail(authorizationHeader.substring(7));
        log.info("Verify payment ", userDetails.get(" name"));
        return ResponseEntity.ok(paymentService.verifyPayment(request, userDetails.get("email")));
    }
}
