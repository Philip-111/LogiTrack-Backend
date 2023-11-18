package org.logitrack.validation;


import org.logitrack.dto.request.AppUserRegistrationRequest;
import org.logitrack.dto.request.ResetPasswordRequest;
import org.logitrack.exceptions.UserExistException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class PasswordValidator {

    public Boolean isValid(AppUserRegistrationRequest userRegistrationRequestDto) {
        String password = userRegistrationRequestDto.getPassword();
        String confirmPassword = userRegistrationRequestDto.getConfirmPassword();

        if (Objects.equals(password, confirmPassword)) {
            return true;
        } else {
            throw new UserExistException("password do not match", HttpStatus.BAD_REQUEST);
        }
    }

    public Boolean isValid(ResetPasswordRequest resetPasswordRequest) {
        String password = resetPasswordRequest.getNewPassword();
        String confirmPassword = resetPasswordRequest.getConfirmPassword();
        if (resetPasswordRequest.getNewPassword().length() <= 5){
            throw new UserExistException("Password must be greater than 6 and less than 20",HttpStatus.BAD_REQUEST);
        }

        if (Objects.equals(password, confirmPassword)) {
            return true;
        } else {
            throw new UserExistException("password do not match", HttpStatus.BAD_REQUEST);
        }
    }
}
