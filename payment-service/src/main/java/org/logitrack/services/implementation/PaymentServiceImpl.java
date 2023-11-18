package org.logitrack.services.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.logitrack.dto.response.InitiatePaymentResponse;
import org.logitrack.dto.response.VerifyPaymentResponse;
import org.logitrack.entities.Transaction;
import org.logitrack.exceptions.TransactionNotFoundException;
import org.logitrack.exceptions.UserNotFoundException;
import org.logitrack.repository.TransactionRepository;
import org.logitrack.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.logitrack.dto.request.PaymentRequest;
import org.logitrack.dto.response.ApiResponse;
import org.logitrack.entities.User;
import org.logitrack.enums.Status;
import org.logitrack.repository.UserRepository;
import org.logitrack.utils.payStack.InitiationData;
import org.logitrack.utils.payStack.PayStackApiConstants;
import org.logitrack.utils.payStack.VerificationData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Value("${PayStackTestSecretKey}")
    private String payStackKey;

    @Override
    public ApiResponse initializePayment(PaymentRequest request, String email) {

        request.setEmail(email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user does not exist"));

        try
        {
            HttpHeaders header = new HttpHeaders();
            header.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            header.setContentType(MediaType.APPLICATION_JSON);
            header.add("Authorization", "Bearer " + payStackKey);

            HttpEntity<String> httpRequest = new HttpEntity<>(new Gson().toJson(request), header);

            var responseEntity = restTemplate.postForEntity(
                (PayStackApiConstants.PAYSTACK_INITIALIZE_PAY), httpRequest, Map.class);

            log.info("Resp-entity: "+ responseEntity.getBody());

            ObjectMapper objectMapper = new ObjectMapper();
            var response = objectMapper.convertValue(responseEntity.getBody(), Map.class);

            InitiationData newInitiationData = objectMapper.convertValue(responseEntity.getBody().get("data"), InitiationData.class);

            InitiatePaymentResponse initiatePaymentResponse = InitiatePaymentResponse.builder()
                    .status((Boolean) response.get("status"))
                    .message((String) response.get("message"))
                    .data(newInitiationData)
                    .build();

            Transaction newTransaction = new Transaction();

            newTransaction.setTransactionReference(initiatePaymentResponse.getData().getReference());
            newTransaction.setUser(user);
            newTransaction.setAmount( request.getAmount());
            newTransaction.setCreatedOn(LocalDate.now());
            newTransaction.setCreatedAt(LocalTime.now());
            newTransaction.setTransactionStatus(Status.IN_PROGRESS);

            transactionRepository.save(newTransaction);

            return ApiResponse.builder()
                    .code("00")
                    .message(initiatePaymentResponse.getMessage())
                    .data(initiatePaymentResponse.getData())
                    .status(initiatePaymentResponse.isStatus()+"")
                    .build();
    }
            catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.info(e.getMessage());
                log.error("Unauthorized request. Please check your credentials.");
                return new ApiResponse<>(e.getMessage());
            } else {
                log.error("HTTP error: " + e.getStatusCode());
                log.error("Response body: " + e.getResponseBodyAsString());
                return new ApiResponse<>(e.getResponseBodyAsString());
            }
        } catch (Exception e) {
            log.error("An error occurred: " + e.getMessage());
            return new ApiResponse<>(e.getMessage());
        }
    }

    @Override
    public ApiResponse verifyPayment(String request, String email) {

        try{
            HttpHeaders header = new HttpHeaders();
            header.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            header.add("Content-Type", "application/json");
            header.add("Authorization", "Bearer " + payStackKey);

            HttpEntity<String> httpRequest = new HttpEntity<>(header);

            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                    org.logitrack.utils.payStack.PayStackApiConstants.PAYSTACK_VERIFY + request,
                    HttpMethod.GET, httpRequest, Map.class);

            ObjectMapper objectMapper = new ObjectMapper();

            var response = objectMapper.convertValue(responseEntity.getBody(), Map.class);
            VerificationData verificationData = objectMapper.convertValue(responseEntity.getBody().get("data"), VerificationData.class);

            VerifyPaymentResponse verifyPaymentResponse = VerifyPaymentResponse.builder()
                    .status((Boolean) response.get("status"))
                    .message((String) response.get("message"))
                    .data(verificationData)
                    .build();


            Transaction transaction = transactionRepository.findTransactionByTransactionReference(verifyPaymentResponse.getData().getReference())
                    .orElseThrow(() -> new TransactionNotFoundException("Transaction does not exist"));

            transaction.setTransactionStatus(Status.SUCCESSFUL);
            transaction.setGatewayResponse(verificationData.getGatewayResponse());
            transaction.setCompletedAt(LocalTime.now());

            transactionRepository.save(transaction);

            return ApiResponse.builder()
                .code("00")
                .status(verifyPaymentResponse.isStatus()+"")
                .message(verifyPaymentResponse.getMessage())
                .data(verifyPaymentResponse.getData())
                .build();
        } catch (
                HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.info(e.getMessage());
                log.error("Unauthorized request. Please check your credentials.");
                return new ApiResponse<>(e.getMessage());
            } else {
                log.error("HTTP error: " + e.getStatusCode());
                log.error("Response body: " + e.getResponseBodyAsString());
                return new ApiResponse<>(e.getResponseBodyAsString());
            }
        } catch (Exception e) {
            log.error("An error occurred: " + e.getMessage());
            return new ApiResponse<>(e.getMessage());
        }
    }

}
