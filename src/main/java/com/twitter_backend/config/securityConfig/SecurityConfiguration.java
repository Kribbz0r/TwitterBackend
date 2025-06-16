package com.twitter_backend.config.securityConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // For testing. Permitting all.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // How school thought me. Need to fix.
        http.csrf().disable();
        http.authorizeRequests().anyRequest().permitAll();
        return http.build();
    }
}
