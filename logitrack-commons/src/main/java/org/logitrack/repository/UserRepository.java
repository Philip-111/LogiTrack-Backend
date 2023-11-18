package org.logitrack.repository;

import org.logitrack.entities.User;
import org.logitrack.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Page<User> findByRole(Role role, Pageable pageable);
    Collection<Object> findAllByRole(Role role);
    List<User> findAllByCreationDateBetween(LocalDateTime localDateTime, LocalDateTime localDateTime1);
    boolean existsByEmail(String mail);
}
