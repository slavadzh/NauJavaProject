package org.dzhabarov.naujavaproject.service;

import lombok.extern.slf4j.Slf4j;
import org.dzhabarov.naujavaproject.dto.UserRegistrationDTO;
import org.dzhabarov.naujavaproject.entity.Role;
import org.dzhabarov.naujavaproject.entity.User;
import org.dzhabarov.naujavaproject.exception.BusinessException;
import org.dzhabarov.naujavaproject.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Бизнес-логика пользователей: регистрация и проверка роли
 */
@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * @param userRepository репозиторий пользователей
     * @param passwordEncoder кодировщик паролей
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Находит пользователя по логину
     *
     * @throws BusinessException если пользователь не найден
     */
    public User findByName(String username) {
        return userRepository.findByName(username)
                .orElseThrow(() -> new BusinessException("Пользователь не найден"));
    }

    /**
     * Регистрирует нового пользователя с ролью ROLE_USER
     *
     * @throws BusinessException если логин или email уже заняты
     */
    public User register(UserRegistrationDTO dto) {
        if (userRepository.existsByName(dto.getName())) {
            throw new BusinessException("Пользователь с таким логином уже существует");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.ROLE_USER);
        user.setRegistrationDate(LocalDate.now());
        User saved = userRepository.save(user);
        log.info("User registered: {}", saved.getName());
        return saved;
    }

    /** Проверяет, является ли пользователь администратором */
    public boolean isAdmin(String username) {
        return findByName(username).getRole() == Role.ROLE_ADMIN;
    }
}
