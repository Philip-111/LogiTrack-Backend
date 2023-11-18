package org.logitrack.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppUserRegistrationRequest {

    @NotBlank(message = "FullName cannot be empty")
//    @Pattern(regexp = "[a-zA-Z]*", message = "FirstName can only have letters")
    @Pattern(regexp = "^[a-zA-Z ]*$", message = "FirstName can only have letters and spaces")
    private String fullName;
    @Email
    @NotBlank(message = "email cannot be empty")
    private String Email;

    @NotBlank(message = " password cannot be empty")
    @Size(message = "Password must be greater than 6 and less than 20", min = 6, max = 20)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).{6,20}$", message = "Password must contain at least one lowercase letter, one uppercase letter, and one number")
    private String password;

    @NotBlank(message = " password cannot be empty")
    private String confirmPassword;

    @NotBlank(message = " phoneNumber cannot be empty")
    @Size(message = "phoneNumber must be atLeast 11",max = 14)
    private String phoneNumber;


}
