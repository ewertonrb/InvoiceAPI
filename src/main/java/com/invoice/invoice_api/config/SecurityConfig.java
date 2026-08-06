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
                                "/public/invitations/decline"
                        ).permitAll()

                        .requestMatchers("/platform/**")
                        .hasAuthority("PLATFORM_ADMIN")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/companies"
                        ).denyAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/public/join-links/accept"
                        ).authenticated()

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
                                "/invoices/*/issue",
                                "/invoices/*/paid",
                                "/invoices/*/cancel"
                        )
                        .hasAnyAuthority("OWNER", "MANAGER")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/projects"
                        )
                        .hasAnyAuthority("OWNER", "ADMIN", "MANAGER")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/projects/*"
                        )
                        .hasAnyAuthority("OWNER", "ADMIN", "MANAGER")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/projects/*/deactivate",
                                "/projects/*/reactivate"
                        )
                        .hasAnyAuthority("OWNER", "ADMIN", "MANAGER")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/projectpositions"
                        )
                        .hasAnyAuthority("OWNER", "ADMIN", "MANAGER")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/projectpositions/*"
                        )
                        .hasAnyAuthority("OWNER", "ADMIN", "MANAGER")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/projectpositions/*/deactivate",
                                "/projectpositions/*/reactivate"
                        )
                        .hasAnyAuthority("OWNER", "ADMIN", "MANAGER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/project-role-rates/**"
                        )
                        .hasAnyAuthority(
                                "OWNER",
                                "ADMIN",
                                "MANAGER",
                                "FINANCE"
                        )

                        .requestMatchers(
                                HttpMethod.POST,
                                "/project-role-rates"
                        )
                        .hasAnyAuthority("OWNER", "ADMIN", "MANAGER")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/project-role-rates/*"
                        )
                        .hasAnyAuthority("OWNER", "ADMIN", "MANAGER")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/project-role-rates/*/deactivate",
                                "/project-role-rates/*/reactivate"
                        )
                        .hasAnyAuthority("OWNER", "ADMIN", "MANAGER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/work-logs/worker/**"
                        )
                        .hasAnyAuthority(
                                "OWNER", "ADMIN", "MANAGER", "FINANCE", "WORKER"
                        )

                        .requestMatchers(
                                HttpMethod.GET,
                                "/work-logs",
                                "/work-logs/",
                                "/work-logs/project/**"
                        )
                        .hasAnyAuthority(
                                "OWNER", "ADMIN", "MANAGER", "FINANCE"
                        )

                        .requestMatchers(
                                HttpMethod.GET,
                                "/work-logs/*"
                        )
                        .hasAnyAuthority(
                                "OWNER", "ADMIN", "MANAGER", "FINANCE", "WORKER"
                        )

                        .requestMatchers(
                                HttpMethod.POST,
                                "/work-logs"
                        )
                        .hasAuthority("WORKER")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/work-logs/*"
                        )
                        .hasAuthority("WORKER")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/work-logs/*/approve",
                                "/work-logs/*/reject"
                        )
                        .hasAnyAuthority("OWNER", "ADMIN", "MANAGER", "FINANCE")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/work-logs/*/cancel",
                                "/work-logs/*/reopen"
                        )
                        .hasAnyAuthority(
                                "OWNER", "ADMIN", "MANAGER", "FINANCE", "WORKER"
                        )

                        .requestMatchers(
                                HttpMethod.GET,
                                "/worker-profiles/me"
                        )
                        .hasAuthority("WORKER")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/worker-profiles/me"
                        )
                        .hasAuthority("WORKER")

                        .requestMatchers(
                                "/companies/*/workers/**",
                                "/companies/*/invitations/**",
                                "/companies/*/join-links/**"
                        )
                        .hasAnyAuthority("OWNER", "ADMIN", "MANAGER")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/company-memberships"
                        ).denyAll()

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/company-memberships/*/role",
                                "/company-memberships/*/deactivate",
                                "/company-memberships/*/reactivate",
                                "/companies/*/memberships/*/role"
                        ).hasAnyAuthority("OWNER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/company-memberships/**"
                        ).hasAnyAuthority("OWNER", "ADMIN", "MANAGER")

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
