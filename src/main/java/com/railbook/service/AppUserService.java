
package com.railbook.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.railbook.entity.AppUser;
import com.railbook.repository.AppUserRepository;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    // REGISTER PASSENGER
    public AppUser register(String username, String password) {

        if (appUserRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists!");
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));

        return appUserRepository.save(user);
    }

    // LOGIN PASSENGER
    public AppUser login(String username, String password) {

        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("Invalid username or password!"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid username or password!");
        }

        return user;
    }
}
