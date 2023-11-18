package org.logitrack.dto.response;
import lombok.Data;
import org.logitrack.enums.Role;
@Data
public class AppUserDetailResponse {
    private String fullName;
    private String email;
    private Role role;
}
