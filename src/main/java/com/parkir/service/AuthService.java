package com.parkir.service;

import com.parkir.exception.LoginGagalException;
import com.parkir.model.User;
import com.parkir.repository.UserRepository;

import java.util.Objects;
import java.util.Optional;

public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null) {
            throw new LoginGagalException("Username atau password salah");
        }
        Optional<User> userOpt = userRepository.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            throw new LoginGagalException("Username atau password salah");
        }
        User user = userOpt.get();
        if (!user.cocokPassword(password)) {
            throw new LoginGagalException("Username atau password salah");
        }
        return user;
    }
}
