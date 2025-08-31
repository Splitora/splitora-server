package com.satwik.splitora.repository;

import com.satwik.splitora.persistence.entities.User;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email = :email OR (u.phoneNumber = :phoneNumber AND u.countryCode = :countryCode)")
    Optional<User> findByEmailOrPhone(String email, Long phoneNumber, @Pattern(regexp = "\\+\\d+") String countryCode);
}
