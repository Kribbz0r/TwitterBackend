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
    private UserService sut;

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

        ApplicationUser savedUser = sut.registerUser(registrationObject);

        assertTrue(savedUser.getUsername().contains(registrationObject.getFirstName()));
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

        ApplicationUser savedUser = sut.registerUser(registrationObject);

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

        assertThrows(NoSuchElementException.class, () -> sut.registerUser(registrationObject));
    }

    @Test
    void givenSaveFails_whenRegisterUser_thenThrowsEmailAlreadyExists() {
        RegistrationObject registrationObject = new RegistrationObject();
        registrationObject.setFirstName("TheDude");
        registrationObject.setLastName("Dudeson");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByAuthority("USER")).thenReturn(Optional.of(new Role(1, "USER")));
        when(userRepository.save(any(ApplicationUser.class))).thenThrow(new RuntimeException());

        assertThrows(EmailAlreadyExistsException.class, () -> sut.registerUser(registrationObject));
    }

    @Test
    void givenExistingUser_whenGenerateUserVerification_thenEmailSentAndUserSaved() throws Exception {
        ApplicationUser user = new ApplicationUser();
        user.setUsername("TheDude");
        user.setEmail("dude@example.com");
        when(userRepository.findByUsername("TheDude")).thenReturn(Optional.of(user));
        doNothing().when(mailService).sendGmail(anyString(), anyString(), anyString());

        sut.generateUserVerification("TheDude");

        assertNotNull(user.getVerification());
        verify(mailService).sendGmail(
                eq("dude@example.com"),
                eq("Your verification code"),
                contains(user.getVerification().toString()));
        verify(userRepository).save(user);
    }

    @Test
    void givenNonExistingUser_whenGenerateUserVerification_thenThrowsUserDoesntExistException() {
        when(userRepository.findByUsername("notTheDude")).thenReturn(Optional.empty());

        assertThrows(UserDoesntExistException.class,
                () -> sut.generateUserVerification("notTheDude"));
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
                () -> sut.generateUserVerification("TheDude"));
        verify(userRepository, never()).save(user);
    }

    @Test
    void givenExistingUser_whenGenerateUserVerification_thenVerificationNumberMatchesPattern() throws Exception {
        ApplicationUser user = new ApplicationUser();
        user.setUsername("TheDude");
        user.setEmail("dude@example.com");
        when(userRepository.findByUsername("TheDude")).thenReturn(Optional.of(user));
        doNothing().when(mailService).sendGmail(anyString(), anyString(), anyString());

        sut.generateUserVerification("TheDude");

        // Note to self. Study matches(). What if its null? This should handle it, no?
        assertTrue(user.getVerification().toString().matches("\\d+"));
    }

    @Test
    void givenValidUser_whenUpdateUser_thenUserIsSaved() throws Exception {
        ApplicationUser user = new ApplicationUser();
        when(userRepository.save(user)).thenReturn(user);

        ApplicationUser updated = sut.updateUser(user);

        assertEquals(user, updated);
    }

    @Test
    void givenSaveFails_whenUpdateUser_thenThrowsEmailAlreadyExists() throws Exception {

        ApplicationUser user = new ApplicationUser();
        when(userRepository.save(user)).thenThrow(IllegalArgumentException.class);

        assertThrows(EmailAlreadyExistsException.class, () -> sut.updateUser(user));
    }

}
