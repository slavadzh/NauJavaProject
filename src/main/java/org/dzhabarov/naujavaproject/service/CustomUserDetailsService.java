package org.dzhabarov.naujavaproject.service;

import org.dzhabarov.naujavaproject.entity.User;
import org.dzhabarov.naujavaproject.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Загрузка пользователя из БД для Spring Security
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * @param userRepository репозиторий пользователей
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Находит пользователя по логину и формирует UserDetails с ролью
     *
     * @param name логин пользователя
     */
    @Override
    public UserDetails loadUserByUsername(String name) {

        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getName())
                .password(user.getPassword())
                .roles(user.getRole().name().replace("ROLE_", ""))
                .build();
    }
}
