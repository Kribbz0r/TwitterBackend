package com.twitter_backend.controllers;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.twitter_backend.exceptions.EmailAlreadyExistsException;
import com.twitter_backend.exceptions.EmailFailedToSendException;
import com.twitter_backend.exceptions.IncorrectVerificationCodeException;
import com.twitter_backend.exceptions.UserDoesntExistException;
import com.twitter_backend.models.ApplicationUser;
import com.twitter_backend.models.RegistrationObject;
import com.twitter_backend.services.UserService;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/authenticate")
@CrossOrigin("*")
public class AuthenticationController {

    private final UserService userService;

    @Autowired
    public AuthenticationController(UserService userService) {
        this.userService = userService;
    }

    @ExceptionHandler({ EmailAlreadyExistsException.class })
    public ResponseEntity<String> handleEmailAlreadyExists() {
        return new ResponseEntity<String>("The provided email already exists", HttpStatus.CONFLICT);
    }

    @ExceptionHandler({ UserDoesntExistException.class })
    public ResponseEntity<String> handleUserDoesntExist() {
        return new ResponseEntity<String>("The user doesn't exist", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({ EmailFailedToSendException.class })
    public ResponseEntity<String> handleFailedToSendEmail() {
        return new ResponseEntity<>("Failed to send email, try again later",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({ IncorrectVerificationCodeException.class })
    public ResponseEntity<String> IncorrectVerificationCodeException() {
        return new ResponseEntity<>("Incorrect verificationcode", HttpStatus.CONFLICT);
    }

    @PostMapping("/register")
    public ApplicationUser registerUser(@RequestBody RegistrationObject registrationObject) {
        return userService.registerUser(registrationObject);
    }

    @PutMapping("/update/phoneNumber")
    public ApplicationUser updatePhoneNumber(@RequestBody LinkedHashMap<String, String> body) {

        String username = body.get("username");
        String phoneNumber = body.get("phoneNumber");

        ApplicationUser user = userService.getUserByUsername(username);

        user.setPhoneNumber(phoneNumber);

        return userService.updateUser(user);

    }

    @PostMapping("/email/verification/code")
    public ResponseEntity<String> createEmailVerification(@RequestBody LinkedHashMap<String, String> body)
            throws Exception {
        try {
            userService.generateUserVerification(body.get("username"));
        } catch (Exception e) {
            e.printStackTrace();

        }

        return new ResponseEntity<String>("Verification code has been sent to your email", HttpStatus.OK);
    }

    @PostMapping("email/verification/verify")
    public ApplicationUser verifyEmail(@RequestBody LinkedHashMap<String, String> body) throws Exception {
        Long code = Long.parseLong(body.get("verificationCode"));
        String username = body.get("username");
        return userService.verifyEmail(username, code);
    }

    @PutMapping("/update/password")
    public ApplicationUser updatePassword(@RequestBody LinkedHashMap<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        return userService.setPassword(username, password);

    }

}
