package com.twitter_backend.services;

import java.net.Authenticator;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.twitter_backend.exceptions.EmailAlreadyExistsException;
import com.twitter_backend.exceptions.EmailFailedToSendException;
import com.twitter_backend.exceptions.IncorrectVerificationCodeException;
import com.twitter_backend.exceptions.UserDoesntExistException;
import com.twitter_backend.models.ApplicationUser;
import com.twitter_backend.models.RegistrationObject;
import com.twitter_backend.models.Role;
import com.twitter_backend.repositories.RoleRepository;
import com.twitter_backend.repositories.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, MailService mailService,
            PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
    }

    public ApplicationUser registerUser(RegistrationObject registrationObject) {

        ApplicationUser user = new ApplicationUser();

        user.setFirstName(registrationObject.getFirstName());
        user.setLastName(registrationObject.getLastName());
        user.setEmail(registrationObject.getEmail());
        user.setDateOfBirth(registrationObject.getDateOfBirth());

        String name = user.getFirstName() + user.getLastName();
        boolean nametaken = true;
        String tempName = "";
        while (nametaken) {
            tempName = generatedUsername(name);
            if (userRepository.findByUsername(tempName).isEmpty()) {
                nametaken = false;
            }
        }

        Set<Role> roles = user.getAuthorities();
        roles.add(roleRepository.findByAuthority("USER").get());
        user.setAuthorities(roles);

        user.setUsername(tempName);

        try {
            return userRepository.save(user);
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmailAlreadyExistsException();
        }

    }

    private String generatedUsername(String name) {

        long generatedNumber = (long) Math.floor(Math.random() * 1_000_000_000);
        return name + generatedNumber;

    }

    public ApplicationUser getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(UserDoesntExistException::new);
    }

    public ApplicationUser updateUser(ApplicationUser user) {
        try {
            return userRepository.save(user);
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmailAlreadyExistsException();
        }
    }

    public void generateUserVerification(String username) throws Exception {
        ApplicationUser user = userRepository.findByUsername(username).orElseThrow(UserDoesntExistException::new);
        user.setVerification(generateVerificationNumber());

        try {
            mailService.sendGmail(user.getEmail(), "Your verification code",
                    "This is your verification code: " + user.getVerification());
            userRepository.save(user);
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmailFailedToSendException();
        }

        userRepository.save(user);
    }

    private Long generateVerificationNumber() {
        return (long) Math.floor(Math.random() * 1_000_000_000);

    }

    public ApplicationUser verifyEmail(String username, Long verificationCode) throws Exception {
        try {
            ApplicationUser user = userRepository.findByUsername(username).orElseThrow(UserDoesntExistException::new);
            if (verificationCode.equals(user.getVerification())) {
                user.setEnabled(true);
                user.setVerification(null);
                return userRepository.save(user);
            } else {
                throw new IncorrectVerificationCodeException();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IncorrectVerificationCodeException();
        }
    }

    public ApplicationUser setPassword(String username, String password) {
        ApplicationUser user = userRepository.findByUsername(username).orElseThrow(UserDoesntExistException::new);
        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);

        return userRepository.save(user);
    }

}
