package com.satwik.splitora.service.implementations;

import com.satwik.splitora.constants.enums.RegistrationMethod;
import com.satwik.splitora.constants.enums.UserRole;
import com.satwik.splitora.exception.BadRequestException;
import com.satwik.splitora.persistence.dto.user.PhoneDTO;
import com.satwik.splitora.persistence.dto.user.RegisterUserRequest;
import com.satwik.splitora.persistence.dto.user.UserDTO;
import com.satwik.splitora.persistence.entities.Group;
import com.satwik.splitora.persistence.entities.User;
import com.satwik.splitora.persistence.entities.UserEvents;
import com.satwik.splitora.repository.GroupRepository;
import com.satwik.splitora.repository.UserRepository;
import com.satwik.splitora.service.interfaces.UserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final AuthorizationService authorizationService;

    private final NotificationService notificationService;

    private final UserRepository userRepository;

    private final GroupRepository groupRepository;

    private final BCryptPasswordEncoder pwdEncoder;

    public UserServiceImpl(AuthorizationService authorizationService, NotificationService notificationService, UserRepository userRepository, GroupRepository groupRepository, BCryptPasswordEncoder pwdEncoder) {
        this.authorizationService = authorizationService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.pwdEncoder = pwdEncoder;
    }

    // for save and update
    @Override
    @Transactional
    public String saveUser(RegisterUserRequest request) {

        validateUserData(request);

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setUserRole(UserRole.USER);
        user.setCountryCode(request.getPhone().getCountryCode());
        user.setPhoneNumber(request.getPhone().getPhoneNumber());
        user.setPassword(pwdEncoder.encode(request.getPassword()));
        user.setRegistrationMethod(RegistrationMethod.NORMAL);

        // create user events
        createUserEvents(user);

        user = userRepository.save(user);

        // create default group
        Group group = new Group();
        group.setGroupName("Non Grouped Expenses");
        group.setUser(user);
        group.setDefaultGroup(true);
        groupRepository.save(group);

        // Send welcome email
        notificationService.sendWelcomeEmail(user);

        // return user id
        return user.getId().toString();
    }

    private void validateUserData(RegisterUserRequest request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("User with this email already exists");
        }
        if(userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already in use");
        }
        if(userRepository.existsByCountryCodeAndPhoneNumber(request.getPhone().getCountryCode(), request.getPhone().getPhoneNumber())) {
            throw new BadRequestException("Phone number already in use");
        }
    }

    // create user events
    private void createUserEvents(User user) {
        UserEvents userEvents = new UserEvents();
        userEvents.setWelcomeEmailSent(false);
        userEvents.setEmailVerified(false);
        userEvents.setPhoneNumberVerified(false);
        user.setUserEvents(userEvents);
    }

    @Override
    public UserDTO findUser() {
        User user = authorizationService.getAuthorizedUser();
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                new PhoneDTO(user.getCountryCode(), user.getPhoneNumber())
        );
    }

    @Override
    @Transactional
    public String deleteUser() {
        User user = authorizationService.getAuthorizedUser();
        userRepository.deleteById(user.getId());
        return "%s - user deleted.".formatted(user.getId());
    }

    @Override
    @Transactional
    public String updateUser(RegisterUserRequest request) {
        User user = authorizationService.getAuthorizedUser();
        if(!user.getEmail().equals(request.getEmail())) throw new AccessDeniedException("You are not authenticated for updating this account");
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setCountryCode(request.getPhone().getCountryCode());
        user.setPhoneNumber(request.getPhone().getPhoneNumber());
        user.setPassword(pwdEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return request.getUsername() + " updated successfully.";
    }

}
