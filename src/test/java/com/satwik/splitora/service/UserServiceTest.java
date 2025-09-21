package com.satwik.splitora.service;

import com.satwik.splitora.constants.enums.UserRole;
import com.satwik.splitora.exception.BadRequestException;
import com.satwik.splitora.persistence.dto.user.PhoneDTO;
import com.satwik.splitora.persistence.dto.user.RegisterUserRequest;
import com.satwik.splitora.persistence.entities.Group;
import com.satwik.splitora.persistence.entities.User;
import com.satwik.splitora.repository.GroupRepository;
import com.satwik.splitora.repository.UserRepository;
import com.satwik.splitora.service.implementations.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterUserRequest request;

    @BeforeEach
    void setUp() {
        request = new RegisterUserRequest();
        request.setUsername("testuser");
        request.setEmail("testuser@example.com");
        request.setPhone(new PhoneDTO("+1", 1234567890L));
        request.setPassword("password123");
    }

    @Test
    void testSaveUser_Success() {
        // arrange - validation checks
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByCountryCodeAndPhoneNumber(request.getPhone().getCountryCode(),
                request.getPhone().getPhoneNumber())).thenReturn(false);

        // arrange - password encoding
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        // when saving user, return user with id
        String uuid = UUID.randomUUID().toString();
        when(userRepository.save(any())).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", UUID.fromString(uuid));
            return user;
        });
        // when saving group, return group
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // act
        String userId = userService.saveUser(request);

        // assert
        assertEquals(uuid, userId);

        // capture the user object passed to save and assert its fields
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(request.getUsername(), savedUser.getUsername());
        assertEquals(request.getEmail(), savedUser.getEmail());
        assertEquals(request.getPhone().getCountryCode(), savedUser.getCountryCode());
        assertEquals(request.getPhone().getPhoneNumber(), savedUser.getPhoneNumber());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals(UserRole.USER, savedUser.getUserRole());

        // capture the group object passed to save and assert its fields
        ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
        verify(groupRepository).save(groupCaptor.capture());
        Group savedGroup = groupCaptor.getValue();
        assertEquals("Non Grouped Expenses", savedGroup.getGroupName());
        assertTrue(savedGroup.isDefaultGroup());
        assertEquals(savedUser, savedGroup.getUser());
    }

    @Test
    void testSaveUser_EmailAlreadyExists() {
        // arrange
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // act & assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> userService.saveUser(request));
        assertEquals("User with this email already exists", exception.getMessage());

        // Ensure no saving happened
        verify(userRepository, never()).save(any());
        verify(groupRepository, never()).save(any());
    }

    @Test
    void testSaveUser_UsernameAlreadyExists() {
        // arrange
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        // act & assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> userService.saveUser(request));
        assertEquals("Username already in use", exception.getMessage());

        // Ensure no saving happened
        verify(userRepository, never()).save(any());
        verify(groupRepository, never()).save(any());
    }

    @Test
    void testSaveUser_PhoneNumberAlreadyExists() {
        // arrange
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByCountryCodeAndPhoneNumber(request.getPhone().getCountryCode(),
                request.getPhone().getPhoneNumber())).thenReturn(true);

        // act & assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> userService.saveUser(request));
        assertEquals("Phone number already in use", exception.getMessage());

        // Ensure no saving happened
        verify(userRepository, never()).save(any());
        verify(groupRepository, never()).save(any());
    }
}
