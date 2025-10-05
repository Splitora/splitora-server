package com.satwik.splitora.service.implementations;

import com.satwik.splitora.configuration.jwt.JwtUtil;
import com.satwik.splitora.exception.BadRequestException;
import com.satwik.splitora.exception.DataNotFoundException;
import com.satwik.splitora.persistence.dto.user.AuthenticationResponse;
import com.satwik.splitora.persistence.dto.user.LoginRequest;
import com.satwik.splitora.persistence.dto.user.RefreshTokenRequest;
import com.satwik.splitora.persistence.entities.User;
import com.satwik.splitora.persistence.entities.UserEvents;
import com.satwik.splitora.repository.UserRepository;
import com.satwik.splitora.service.interfaces.AuthService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;

    private final UserRepository userRepository;

    private final NotificationService notificationService;

    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(JwtUtil jwtUtil, UserRepository userRepository, NotificationService notificationService, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @Override
    public AuthenticationResponse authenticateUser(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new DataNotFoundException("User not found."));
        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid User mail or password!");
        }
        String token = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        sendWelcomeEmail(user);

        return new AuthenticationResponse(token, refreshToken, "Successfully generated token!");
    }

    private void sendWelcomeEmail(User user) {
        UserEvents userEvents = user.getUserEvents();
        if (userEvents == null) {
            log.error("UserEvents not found for user: {}, hence, event is not known!", user.getEmail());
            return;
        }

        boolean isWelcomeEmailSent = userEvents.isWelcomeEmailSent();
        if(!isWelcomeEmailSent) {
            notificationService.sendWelcomeEmail(user);
        }
    }

    @Override
    public AuthenticationResponse issueNewToken(RefreshTokenRequest refreshTokenRequest) {
        String userId = jwtUtil.getClaimsOfRefreshToken(refreshTokenRequest.getRefreshToken()).getSubject();
        User user = userRepository.findByEmail(userId).orElseThrow(() -> new DataNotFoundException("User not found."));
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        return new AuthenticationResponse(accessToken, refreshToken, "Successfully generated token from refresh!");
    }
}
