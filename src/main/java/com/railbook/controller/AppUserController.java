
package com.railbook.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.railbook.entity.AppUser;
import com.railbook.service.AppUserService;

@RestController
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    // PASSENGER REGISTRATION
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            if (request.getUsername() == null ||
                request.getUsername().isBlank() ||
                request.getPassword() == null ||
                request.getPassword().length() < 8) {

                return ResponseEntity.badRequest()
                    .body("Enter a username and password of at least 8 characters.");
            }

            appUserService.register(
                request.getUsername().trim(),
                request.getPassword()
            );

            return ResponseEntity.ok("Registration successful!");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PASSENGER LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody RegisterRequest request) {
        try {
            if (request.getUsername() == null ||
                request.getPassword() == null) {
                return ResponseEntity.badRequest()
                    .body("Username and password are required.");
            }

            AppUser user = appUserService.login(
                request.getUsername().trim(),
                request.getPassword()
            );

            return ResponseEntity.ok(
                "Login successful! Welcome " + user.getUsername()
            );

        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    public static class RegisterRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
