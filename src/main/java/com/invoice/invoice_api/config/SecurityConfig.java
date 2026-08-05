package com.invoice.invoice_api.config;

import com.invoice.invoice_api.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                HttpMethod.POST,
                                "/auth/login",
                                "/users",
                                "/public/invitations/accept",
                                "/public/invitations/decline",
                                "/public/join-links/accept"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/public/invitations",
                                "/public/invitations/**",
                                "/public/join-links",
                                "/public/join-links/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/invoices/**"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/invoices/drafts"
                        )
                        .hasAnyAuthority("OWNER", "MANAGER")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/invoices/*/issue"
                        )
                        .hasAnyAuthority("OWNER", "MANAGER")

                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

