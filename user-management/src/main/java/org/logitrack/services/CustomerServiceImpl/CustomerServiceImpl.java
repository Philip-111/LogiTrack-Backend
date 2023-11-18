package org.logitrack.services.CustomerServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.logitrack.dto.MessagingObject;
import org.logitrack.dto.Purpose;
import org.logitrack.dto.request.AppUserLoginRequest;
import org.logitrack.dto.request.AppUserRegistrationRequest;
import org.logitrack.dto.request.DeliveryManCreationRequest;
import org.logitrack.dto.request.ResetPasswordRequest;
import org.logitrack.dto.response.ApiResponse;
import org.logitrack.dto.response.AppUserDetailResponse;
import org.logitrack.dto.response.LoginResponse;
import org.logitrack.emails.EmailService;
import org.logitrack.entities.DeliveryMan;
import org.logitrack.entities.User;
import org.logitrack.entities.VerificationToken;
import org.logitrack.enums.Role;
import org.logitrack.exceptions.CommonApplicationException;
import org.logitrack.exceptions.UserExistException;
import org.logitrack.repository.TokenRepository;
import org.logitrack.repository.UserRepository;
import org.logitrack.securities.JWTService;
import org.logitrack.services.CustomerService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {
    private final JWTService jwtService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${exchange-name}")
    private String exchangeName;

    @Value("${user-registration-routing-key}")
    private String userRegistrationRoutingKey;

    @Value("${delivery-man-creation-routing-key}")
    private String deliveryManCreationRoutingKey;

    @Override
    public ApiResponse registerUser(AppUserRegistrationRequest registrationDTO) {
        Optional<User> optionalUser = userRepository.findByEmail(registrationDTO.getEmail());
        if (optionalUser.isPresent()) {
            return new ApiResponse("00", "User Already Exist", HttpStatus.ALREADY_REPORTED, "success");
        }
        User newUser = new User();
        newUser.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        newUser.setFullName(registrationDTO.getFullName());
        newUser.setPhoneNumber(registrationDTO.getPhoneNumber());
        newUser.setIsVerified(false);
        newUser.setEmail(registrationDTO.getEmail());
        newUser.setRole(Role.CUSTOMER);
        User savedUser = userRepository.save(newUser);
        log.info("user saved to database... about generating email");
        VerificationToken confirmationToken = new VerificationToken(savedUser);
        tokenRepository.save(confirmationToken);
        log.info("verification token generated...");
        String message = "Account created successfully for " + savedUser.getFullName()
                + "Please check your email to verify your account";
        MessagingObject msg = MessagingObject.builder().message(message)
                .destinationEmail(newUser.getEmail())
                .purpose(Purpose.USER_REGISTRATION)
                .title("New Customer Created").build();
        notifyService(msg, userRegistrationRoutingKey);
        String confirmationLink = confirmationToken.getConfirmationToken();

        emailService.sendConfirmationEmail(savedUser, confirmationLink);
        //todo: push to queue..
        log.info("email sent successfully " + confirmationLink);
        ApiResponse genericResponse = new ApiResponse<>();
        genericResponse.setMessage("Registration Successful, Please check your email to verify your account");
        genericResponse.setStatus("Success");
        genericResponse.setCode("00");
        return genericResponse;
    }
    @Override
    public ApiResponse registerDeliveryMan(DeliveryManCreationRequest request, String email) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isPresent()) {
            return new ApiResponse("00", "Rider Already Exist", HttpStatus.ALREADY_REPORTED, "success");
        }
        DeliveryMan newDelivery = new DeliveryMan();
        newDelivery.setFullName(request.getFullName());
        newDelivery.setPhoneNumber(request.getPhoneNumber());
        newDelivery.setIsVerified(true);
        newDelivery.setEmail(request.getEmail());
        newDelivery.setRole(Role.DELIVERY_MAN);
        newDelivery.setBirthday(request.getBirthday());
        newDelivery.setCity(request.getCity());
        newDelivery.setAddress(request.getAddress());
        newDelivery.setGender(request.getGender());
        newDelivery.setPassword(passwordEncoder.encode(request.getPassword()));
        newDelivery.setDrivingLicenseNumber(request.getDrivingLicenseNumber());
        newDelivery.setStateOfIssue(request.getStateOfIssue());
        userRepository.save(newDelivery);
        String message =  "Dear " + newDelivery.getFullName()
                + " your account has been created Successfully with the following details: Email: "
                + request.getEmail() + " and password: " + request.getPassword();
        MessagingObject msg = MessagingObject.builder()
                .message(message)
                .destinationEmail(newDelivery.getEmail())
                .purpose(Purpose.DELIVERY_MAN_CREATION)
                .title("New Delivery Man Created")
                .build();
        notifyService(msg, deliveryManCreationRoutingKey);
        ApiResponse genericResponse = new ApiResponse();
        genericResponse.setMessage("You have successfully created a rider");
        genericResponse.setStatus("Success");
        genericResponse.setCode("00");
        return genericResponse;

    }
    private void notifyService(MessagingObject message, String routingKey) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend(exchangeName, routingKey, jsonMessage);
            log.info("Order notification pushed successfully");
        } catch (JsonProcessingException e) {
            log.error("Error serializing the message: {}", e.getMessage());
        }
    }
    @Override
    public ApiResponse<LoginResponse> login(AppUserLoginRequest loginDTO) {
        log.info("Request to login at the service layer");

        Authentication authenticationUser;
        try {
            authenticationUser = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword())
            );
            log.info("Authenticated the User by the Authentication manager");
        } catch (DisabledException es) {
            return Stream.of(
                            new AbstractMap.SimpleEntry<>("message", "Disabled exception occurred"),
                            new AbstractMap.SimpleEntry<>("status", "failure"),
                            new AbstractMap.SimpleEntry<>("httpStatus", HttpStatus.BAD_REQUEST)
                    )
                    .collect(
                            Collectors.collectingAndThen(
                                    Collectors.toMap(AbstractMap.SimpleEntry::getKey, entry -> entry.getValue()),
                                    map -> new ApiResponse<>((Map<String, String>) map)
                            )
                    );

        } catch (BadCredentialsException e) {
            throw new UserExistException("Invalid email or password", HttpStatus.BAD_REQUEST);
        }
        // Tell securityContext that this user is in the context
        SecurityContextHolder.getContext().setAuthentication(authenticationUser);
        // Retrieve the user from the repository
        User appUser = userRepository.findByEmail(loginDTO.getEmail()).orElseThrow(() ->
                new UserExistException("User not found", HttpStatus.BAD_REQUEST));
        // Update the lastLoginDate field
        appUser.setLastLogin(LocalDateTime.now());
        log.info("last-login date updated");
        // Save the updated user entity
        User user = userRepository.save(appUser);
        log.info("user saved back to database");
        // Generate and send token
        String tokenGenerated = "Bearer " + jwtService.generateToken(authenticationUser, user.getRole());
        log.info("Jwt token generated for the user " + tokenGenerated);
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(tokenGenerated);
        ApiResponse<LoginResponse> apiResponse = new ApiResponse<>("00", "Success", loginResponse, "Successfully logged in", HttpStatus.OK);

        apiResponse.setData(loginResponse);

        return apiResponse;
    }

    @Override
    public ApiResponse regenerateVerificationTokenAndSendEmail(String email) {
        // Find the user by email
        log.info("Regeneration started");
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            log.info("User not in database");
        }
        User existingUser = optionalUser.get();
        log.info("user found in database" + existingUser.getEmail());
        verifyOTPGenerate(existingUser);
        ApiResponse genericResponse = new ApiResponse();
        genericResponse.setMessage("Token resent successfully, Please check your email to verify your account");
        genericResponse.setStatus("Success");
        genericResponse.setCode("00");
        genericResponse.setHttpStatus(HttpStatus.OK);
        return genericResponse;
    }

    public Page<User> fetchByRole(Role role, Pageable pageable, String email) {
        return userRepository.findByRole(role, pageable);
    }

    public ApiResponse verifyEmailAndGenerateOTP(String email) {
        log.info("Reset Password Started");
        Optional<User> optional = userRepository.findByEmail(email);
        if (optional.isEmpty()) {
            log.info("Incorrect email or Invalid User");
            ApiResponse genericResponse = new ApiResponse();
            genericResponse.setMessage("Incorrect Email or Invalid Email");
            genericResponse.setStatus("Error");
            genericResponse.setCode("404");
            genericResponse.setHttpStatus(HttpStatus.BAD_REQUEST);
            return genericResponse;
        }
        User user = optional.get();
        log.info("user found in database" + user.getEmail());
        verifyOTPGenerate(user);
        ApiResponse genericResponse = new ApiResponse();
        genericResponse.setMessage("Reset password token sent successfully, Please check your email to verify your account");
        genericResponse.setStatus("Success");
        genericResponse.setCode("00");
        genericResponse.setHttpStatus(HttpStatus.OK);
        return genericResponse;
    }

    public void verifyOTPGenerate(User user) {
        Optional<VerificationToken> optionalToken = tokenRepository.findTokenByEmail(user.getEmail());
        if (optionalToken.isEmpty()) {
            log.info("No existing token found for the user");
        } else {
            VerificationToken existingToken = optionalToken.get();
            log.info("Existing token retrieved: " + existingToken.getConfirmationToken());
            tokenRepository.delete(existingToken);
            log.info("Existing token deleted");
            VerificationToken newToken = new VerificationToken(user);
            tokenRepository.save(newToken);
            log.info("New token generated: " + newToken);
            user.setVerificationToken(newToken);
            String confirmationLink = user.getVerificationToken().getConfirmationToken();

            emailService.sendConfirmationEmail(user, confirmationLink);
            log.info("Email sent successfully" + confirmationLink);
        }

    }

    public ApiResponse verifyOtp1(String confirmationToken) {
        log.info("Verifying OTP");
        Optional<VerificationToken> optionalToken = tokenRepository.findTokenByConfirmationToken(confirmationToken);

        if (optionalToken.isEmpty()) {
            return createErrorResponse("Invalid OTP", HttpStatus.UNAUTHORIZED);
        }
        ApiResponse response = new ApiResponse();
        response.setMessage("OTP is valid");
        response.setData(optionalToken.get().getEmail());
        response.setStatus("Success");
        response.setCode("200");
        response.setHttpStatus(HttpStatus.OK);
        return response;
    }
    public ApiResponse resetPassword(ResetPasswordRequest request, String email) {
        log.info("Resetting Password");

        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user = optionalUser.get();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        ApiResponse response = new ApiResponse();
        response.setMessage("Password changed successfully");
        response.setStatus("Success");
        response.setCode("200");
        response.setHttpStatus(HttpStatus.OK);
        return response;
    }
    private ApiResponse createErrorResponse(String message, HttpStatus httpStatus) {
        ApiResponse response = new ApiResponse();
        response.setMessage(message);
        response.setStatus("Error");
        response.setCode(String.valueOf(httpStatus.value()));
        response.setHttpStatus(httpStatus);
        return response;
    }

    @Override
    public AppUserDetailResponse getUserDetails(String authorizationHeader) throws CommonApplicationException {
        String token = authorizationHeader.substring(7);
        Map<String, String> userDetails = jwtService.validateTokenAndReturnDetail(token);

        String userEmail = userDetails.get("email");
        String userName = userDetails.get("name");
        Role userRole = Role.valueOf(userDetails.get("role"));

        AppUserDetailResponse detailResponse = new AppUserDetailResponse();
        detailResponse.setFullName(userName);
        detailResponse.setEmail(userEmail);
        detailResponse.setRole(userRole);

        return detailResponse;
    }

    @Override
    public ApiResponse<String> logout(HttpServletRequest request, String authorizationHeader) throws CommonApplicationException {
        String token = authorizationHeader.substring(7);
        Map<String, String> userDetails = jwtService.validateTokenAndReturnDetail(token);
        log.info("Request to logout");
        try {
            SecurityContextHolder.getContext().setAuthentication(null);
            return new ApiResponse<>("00", "Success", "Successfully logged out", "You have been logged out", HttpStatus.OK);
        } catch (Exception e) {
            return new ApiResponse<>("01", "Failure", "Logout failed", "An error occurred while logging out", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
