package com.journeo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable() // désactive CSRF pour simplifier le test
            .authorizeHttpRequests()
                // autoriser Swagger et OpenAPI
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/api/users/ping"
                ).permitAll()
                .anyRequest().authenticated()
            .and()
            .formLogin(); // garde le login form pour les autres endpoints

        return http.build();
    }
}