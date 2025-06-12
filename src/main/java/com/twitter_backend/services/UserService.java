package com.twitter_backend.services;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import com.twitter_backend.exceptions.EmailAlreadyExistsException;
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

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;

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
            throw new EmailAlreadyExistsException();
        }
    }

}
