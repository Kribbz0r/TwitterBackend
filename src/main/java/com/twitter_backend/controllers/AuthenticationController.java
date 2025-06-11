package com.twitter_backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.twitter_backend.exceptions.EmailAlreadyExistsException;
import com.twitter_backend.models.ApplicationUser;
import com.twitter_backend.models.RegistrationObject;
import com.twitter_backend.services.UserService;

@RestController
@RequestMapping("/authenticate")
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

    @PostMapping("/register")
    public ApplicationUser registerUser(@RequestBody RegistrationObject registrationObject) {
        return userService.registerUser(registrationObject);
    }

}
