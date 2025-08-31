package com.satwik.splitora.repository;

import com.satwik.splitora.persistence.entities.UnregisteredUser;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UnregisteredUserRepository extends JpaRepository<UnregisteredUser, UUID> {

    Optional<UnregisteredUser> findByEmail(String email);

    @Query("SELECT u FROM UnregisteredUser u WHERE u.email = :email OR (u.phoneNumber = :phoneNumber AND u.countryCode = :countryCode)")
    Optional<UnregisteredUser> findByEmailOrPhone(String email, Long phoneNumber, @Pattern(regexp = "\\+\\d+") String countryCode);
}
