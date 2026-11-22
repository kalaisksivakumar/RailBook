
package com.railbook.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.railbook.entity.AppUser;
import com.railbook.repository.AppUserRepository;

@Configuration
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService(
            AppUserRepository appUserRepository) {

        return username -> {

            if ("admin".equals(username)) {
                return User.withUsername("admin")
                        .password("{noop}RailBook@123")
                        .roles("ADMIN")
                        .build();
            }

            AppUser passenger = appUserRepository
                    .findByUsername(username)
                    .orElseThrow(() ->
                            new UsernameNotFoundException("User not found"));

            return User.withUsername(passenger.getUsername())
                    .password("{bcrypt}" + passenger.getPassword())
                    .roles("USER")
                    .build();
        };
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {

        return (request, response, authentication) -> {

            boolean isAdmin = authentication.getAuthorities()
                    .stream()
                    .anyMatch(authority ->
                            authority.getAuthority().equals("ROLE_ADMIN"));

            if (isAdmin) {
                response.sendRedirect("/admin.html");
            } else {
                response.sendRedirect("/index.html");
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                    "/",
                    "/index.html",
                    "/style.css",
                    "/script.js",
                    "/login.html",
                    "/login.js",
                    "/register.html",
                    "/register.js",
                    "/api/users/register",
                    "/api/users/login",
                    "/passenger.html",
                    "/passenger.js",
                    "/confirmation.html",
                    "/confirmation.js",
                    "/my-bookings.html",
                    "/my-bookings.js",
                    "/booking-history.html",
                    "/booking-history.js",
                    "/admin-login.html",
                    "/admin-login.js"
                ).permitAll()

                .requestMatchers("/api/trains").permitAll()

                // Single booking
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/bookings"
                ).permitAll()

                // Group booking - NEW
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/bookings/group"
                ).permitAll()

                // Booked seats
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/bookings/train/*/seats"
                ).permitAll()

                // Single PNR lookup
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/bookings/*"
                ).permitAll()

                // Group PNR lookup - NEW
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/bookings/*/group"
                ).permitAll()

                // Cancel booking
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/bookings/*/cancel"
                ).permitAll()

                // Admin access
                .requestMatchers(
                    "/admin.html",
                    "/admin.js",
                    "/api/admin/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/bookings"
                ).hasRole("ADMIN")

                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .successHandler(authenticationSuccessHandler())
                .failureUrl("/login.html?error=true")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutSuccessUrl("/login.html?logout")
                .permitAll()
            );

        return http.build();
    }
}
