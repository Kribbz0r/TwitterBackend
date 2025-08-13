package com.twitter_backend.services;

import com.twitter_backend.exceptions.EmailAlreadyExistsException;
import com.twitter_backend.exceptions.EmailFailedToSendException;
import com.twitter_backend.exceptions.UserDoesntExistException;
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
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private ApplicationUser applicationUser;
    @Mock
    private MailService mailService;

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

    @Test
    void givenExistingUser_whenGenerateUserVerification_thenEmailSentAndUserSavedOnce() throws Exception {
        ApplicationUser user = new ApplicationUser();
        user.setUsername("TheDude");
        user.setEmail("dude@example.com");
        when(userRepository.findByUsername("TheDude")).thenReturn(Optional.of(user));
        doNothing().when(mailService).sendGmail(anyString(), anyString(), anyString());

        userService.generateUserVerification("TheDude");

        assertNotNull(user.getVerification());
        verify(mailService).sendGmail(
                eq("dude@example.com"),
                eq("Your verification code"),
                contains(user.getVerification().toString()));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenNonExistingUser_whenGenerateUserVerification_thenThrowsUserDoesntExistException() {
        when(userRepository.findByUsername("notTheDude")).thenReturn(Optional.empty());

        assertThrows(UserDoesntExistException.class,
                () -> userService.generateUserVerification("notTheDude"));
        verifyNoInteractions(mailService);
        verify(userRepository, never()).save(any());
    }

    @Test
    void givenExistingUser_whenEmailFails_thenThrowsEmailFailedToSendException() throws Exception {
        ApplicationUser user = new ApplicationUser();
        user.setUsername("TheDude");
        user.setEmail("dude@example.com");
        when(userRepository.findByUsername("TheDude")).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("Mail error"))
                .when(mailService).sendGmail(anyString(), anyString(), anyString());

        assertThrows(EmailFailedToSendException.class,
                () -> userService.generateUserVerification("TheDude"));
        verify(userRepository, never()).save(user);
    }

    @Test
    void givenExistingUser_whenGenerateUserVerification_thenVerificationNumberMatchesPattern() throws Exception {
        ApplicationUser user = new ApplicationUser();
        user.setUsername("TheDude");
        user.setEmail("dude@example.com");
        when(userRepository.findByUsername("TheDude")).thenReturn(Optional.of(user));
        doNothing().when(mailService).sendGmail(anyString(), anyString(), anyString());

        userService.generateUserVerification("TheDude");

        // Note to self. Study matches(). What if its null? This should handle it, no?
        assertTrue(user.getVerification().toString().matches("\\d+"));
    }
}
