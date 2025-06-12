package com.twitter_backend;

import java.util.HashSet;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.twitter_backend.models.ApplicationUser;
import com.twitter_backend.models.Role;
import com.twitter_backend.repositories.RoleRepository;
import com.twitter_backend.repositories.UserRepository;
import com.twitter_backend.services.UserService;

@SpringBootApplication
public class TwitterBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TwitterBackendApplication.class, args);
	}

	@Bean
	CommandLineRunner run(RoleRepository roleRepository, UserService userService) {
		return args -> {
			// roleRepository.save(new Role(1, "USER"));

			// create user in database with service

			// ApplicationUser user = new ApplicationUser();
			// user.setFirstName("Dude");
			// user.setLastName("Duder");
			// userService.registerUser(user);

			// Create user in database without service

			// ApplicationUser user = new ApplicationUser();
			// user.setFirstName("Dude");
			// user.setLastName("DudeSon");
			// HashSet<Role> roles = new HashSet<>();
			// roles.add(roleRepository.findByAuthority("USER").get());
			// user.setAuthorities(roles);
			// userRepository.save(user);
		};
	}

}
