package org.dzhabarov.naujavaproject.service;

import org.dzhabarov.naujavaproject.entity.Role;
import org.dzhabarov.naujavaproject.entity.User;
import org.dzhabarov.naujavaproject.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User findByName(String username) {
        return userRepository.findByName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.ROLE_USER);
        return userRepository.save(user);
    }
}
