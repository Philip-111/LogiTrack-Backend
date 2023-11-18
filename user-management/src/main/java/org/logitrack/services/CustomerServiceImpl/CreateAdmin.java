package org.logitrack.services.CustomerServiceImpl;

import lombok.extern.slf4j.Slf4j;
import org.logitrack.entities.Admin;
import org.logitrack.enums.Role;
import org.logitrack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Configuration
@Slf4j
@Service
public class CreateAdmin {
    private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    public CreateAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder=passwordEncoder;
        runAtStart();
    }
    public void runAtStart() {
        log.info("Creating admin");
        if(!userRepository.existsByEmail("AdminOne@gmail.com")) {
            Admin admin = new Admin();
            admin.setEmail("AdminOne@gmail.com");
            admin.setPassword(passwordEncoder.encode("OneAdmin246"));
            admin.setRole(Role.ADMIN);
            admin.setCreationDate(LocalDateTime.now());
            admin.setLastLogin(LocalDateTime.now());
            admin.setPhoneNumber("09039156872");
            admin.setFullName("David Black");
            admin.setIsVerified(true);
            userRepository.save(admin);
        }
    }
}
