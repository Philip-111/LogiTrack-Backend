package org.logitrack.repository;

import org.logitrack.entities.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

;import java.util.Optional;

public interface TokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByConfirmationToken(String confirmationToken);
    Optional<VerificationToken> findTokenByEmail(String userEmail);
    Optional<VerificationToken> findTokenByConfirmationToken(String confirmationToken);
}
