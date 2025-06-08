package com.twitter_backend.services;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import com.twitter_backend.models.ApplicationUser;
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

    public ApplicationUser registerUser(ApplicationUser user) {
        Set<Role> roles = user.getAuthorities();
        roles.add(roleRepository.findByAuthority("USER").get());
        user.setAuthorities(roles);

        return userRepository.save(user);
    }
}
