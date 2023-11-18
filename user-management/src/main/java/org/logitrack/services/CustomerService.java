package org.logitrack.services;

import jakarta.servlet.http.HttpServletRequest;
import org.logitrack.dto.request.AppUserLoginRequest;
import org.logitrack.dto.request.AppUserRegistrationRequest;
import org.logitrack.dto.request.DeliveryManCreationRequest;
import org.logitrack.dto.request.ResetPasswordRequest;
import org.logitrack.dto.response.ApiResponse;
import org.logitrack.dto.response.AppUserDetailResponse;
import org.logitrack.dto.response.LoginResponse;
import org.logitrack.entities.User;
import org.logitrack.enums.Role;
import org.logitrack.exceptions.CommonApplicationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    ApiResponse registerUser(AppUserRegistrationRequest registrationDTO);
    ApiResponse<LoginResponse> login(AppUserLoginRequest loginDTO);
    ApiResponse registerDeliveryMan(DeliveryManCreationRequest request, String email);
    ApiResponse regenerateVerificationTokenAndSendEmail(String email);
    Page<User> fetchByRole(Role role, Pageable pageable, String email);
    ApiResponse verifyOtp1(String confirmationToken);
    ApiResponse verifyEmailAndGenerateOTP(String email);
    ApiResponse resetPassword(ResetPasswordRequest request, String email);
    AppUserDetailResponse getUserDetails(String authorizationHeader) throws CommonApplicationException;

    ApiResponse<String> logout(HttpServletRequest request, String authorizationHeader) throws CommonApplicationException;
}
