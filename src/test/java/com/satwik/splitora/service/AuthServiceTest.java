package com.satwik.splitora.service;

import com.satwik.splitora.configuration.jwt.JwtUtil;
import com.satwik.splitora.persistence.dto.user.AuthenticationResponse;
import com.satwik.splitora.persistence.dto.user.LoginRequest;
import com.satwik.splitora.persistence.entities.User;
import com.satwik.splitora.persistence.entities.UserEvents;
import com.satwik.splitora.repository.UserRepository;
import com.satwik.splitora.service.implementations.AuthServiceImpl;
import com.satwik.splitora.service.implementations.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private BCryptPasswordEncoder passwordEncode;

    @Test
    void testAuthenticateUser_Success() {
        // arrange - preparing login request
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("testuser@example.com");
        loginRequest.setPassword("password123");

        User mockUser = new User();
        mockUser.setEmail("testuser@example.com");
        mockUser.setPassword("encodedPassword");

        UserEvents mockEvents = new UserEvents();
        mockEvents.setWelcomeEmailSent(false);
        mockUser.setUserEvents(mockEvents);


        // arrange - validation checks
        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncode.matches("password123", mockUser.getPassword())).thenReturn(true);

        when(jwtUtil.generateAccessToken(mockUser)).thenReturn("dummyAccessToken");
        when(jwtUtil.generateRefreshToken(mockUser)).thenReturn("dummyRefreshToken");

        // act
        AuthenticationResponse response = authService.authenticateUser(loginRequest);

        // assert
        assertNotNull(response);
        assertEquals("dummyAccessToken", response.getAccessToken());
        assertEquals("dummyRefreshToken", response.getRefreshToken());
        assertEquals("Successfully generated token!", response.getMessage());

        // verify email sent
        verify(notificationService, times(1)).sendWelcomeEmail(mockUser);
    }
}
