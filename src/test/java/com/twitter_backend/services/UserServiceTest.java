package com.twitter_backend.services;

import com.twitter_backend.exceptions.EmailAlreadyExistsException;
import com.twitter_backend.models.ApplicationUser;
import com.twitter_backend.models.RegistrationObject;
import com.twitter_backend.models.Role;
import com.twitter_backend.repositories.RoleRepository;
import com.twitter_backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Date;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private ApplicationUser applicationUser;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void givenValidRegistration_whenRegisterUser_thenUserIsSaved() {

        RegistrationObject registrationObject = new RegistrationObject();
        registrationObject.setFirstName("TheDude");
        registrationObject.setLastName("Dudeson");
        registrationObject.setEmail("dude@example.com");
        registrationObject.setDateOfBirth(Date.valueOf("1970-01-01"));

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByAuthority("USER")).thenReturn(Optional.of(new Role(1, "USER")));
        when(userRepository.save(any(ApplicationUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationUser savedUser = userService.registerUser(registrationObject);

        assertNotNull(savedUser.getUsername());
        assertTrue(savedUser.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("USER")));
        verify(userRepository, atLeastOnce()).save(any(ApplicationUser.class));
    }

    @Test
    void givenUsernameCollision_whenRegisterUser_thenTriesAgain() {
        RegistrationObject registrationObject = new RegistrationObject();
        registrationObject.setFirstName("TheDude");
        registrationObject.setLastName("Dudeson");

        when(userRepository.findByUsername(anyString()))
                .thenReturn(Optional.of(new ApplicationUser()))
                .thenReturn(Optional.empty());

        when(roleRepository.findByAuthority("USER")).thenReturn(Optional.of(new Role(1, "USER")));
        when(userRepository.save(any(ApplicationUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationUser savedUser = userService.registerUser(registrationObject);

        assertNotNull(savedUser.getUsername());
        verify(userRepository, times(2)).findByUsername(anyString());
    }

    @Test
    void givenRoleNotFound_whenRegisterUser_thenThrowsNoSuchElement() {
        RegistrationObject registrationObject = new RegistrationObject();
        registrationObject.setFirstName("TheDude");
        registrationObject.setLastName("Dudeson");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByAuthority("USER")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.registerUser(registrationObject));
    }

    @Test
    void givenSaveFails_whenRegisterUser_thenThrowsEmailAlreadyExists() {
        RegistrationObject registrationObject = new RegistrationObject();
        registrationObject.setFirstName("TheDude");
        registrationObject.setLastName("Dudeson");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByAuthority("USER")).thenReturn(Optional.of(new Role(1, "USER")));
        when(userRepository.save(any(ApplicationUser.class))).thenThrow(new RuntimeException());

        assertThrows(EmailAlreadyExistsException.class, () -> userService.registerUser(registrationObject));
    }

}
