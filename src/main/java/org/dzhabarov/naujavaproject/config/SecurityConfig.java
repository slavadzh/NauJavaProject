package org.dzhabarov.naujavaproject.config;

import lombok.RequiredArgsConstructor;
import org.dzhabarov.naujavaproject.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Настройка Spring Security: роли, URL-доступ, вход и выход
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    /** Кодировщик паролей BCrypt */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Цепочка фильтров безопасности и правила доступа */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/register", "/login").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/books/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/library/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                        .hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("name")
                        .defaultSuccessUrl("/books", true)
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/login")
                        .permitAll()
                );

        return http.build();
    }
}
