package org.dzhabarov.naujavaproject.service;

import org.dzhabarov.naujavaproject.dto.UserRegistrationDTO;
import org.dzhabarov.naujavaproject.entity.Role;
import org.dzhabarov.naujavaproject.entity.User;
import org.dzhabarov.naujavaproject.exception.BusinessException;
import org.dzhabarov.naujavaproject.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты {@link UserService}: регистрация, поиск, проверка роли
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRegistrationDTO registrationDto;

    @BeforeEach
    void setUp() {
        registrationDto = new UserRegistrationDTO();
        registrationDto.setName("ivanov");
        registrationDto.setEmail("ivanov@mail.ru");
        registrationDto.setPassword("secret");
    }

    /**
     * Успешная регистрация с ролью USER и хэшированным паролем
     */
    @Test
    void register_savesUserWithEncodedPassword_whenDataValid() {
        when(userRepository.existsByName("ivanov")).thenReturn(false);
        when(userRepository.existsByEmail("ivanov@mail.ru")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        User result = userService.register(registrationDto);

        assertEquals(Role.ROLE_USER, result.getRole());
        assertEquals("encoded", result.getPassword());
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("ivanov", captor.getValue().getName());
    }

    /**
     * Дубликат логина при регистрации запрещён
     */
    @Test
    void register_throws_whenNameAlreadyExists() {
        when(userRepository.existsByName("ivanov")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.register(registrationDto));
        assertEquals("Пользователь с таким логином уже существует", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    /**
     * Дубликат email при регистрации запрещён
     */
    @Test
    void register_throws_whenEmailAlreadyExists() {
        when(userRepository.existsByName("ivanov")).thenReturn(false);
        when(userRepository.existsByEmail("ivanov@mail.ru")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.register(registrationDto));
        assertEquals("Пользователь с таким email уже существует", ex.getMessage());
    }

    /**
     * Поиск несуществующего пользователя — ошибка
     */
    @Test
    void findByName_throws_whenUserNotFound() {
        when(userRepository.findByName("unknown")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> userService.findByName("unknown"));
    }

    /**
     * isAdmin возвращает true для ROLE_ADMIN
     */
    @Test
    void isAdmin_returnsTrue_forAdminRole() {
        User admin = new User();
        admin.setName("admin");
        admin.setRole(Role.ROLE_ADMIN);
        when(userRepository.findByName("admin")).thenReturn(Optional.of(admin));

        assertTrue(userService.isAdmin("admin"));
    }

    /**
     * isAdmin возвращает false для обычного читателя
     */
    @Test
    void isAdmin_returnsFalse_forUserRole() {
        User reader = new User();
        reader.setName("reader");
        reader.setRole(Role.ROLE_USER);
        when(userRepository.findByName("reader")).thenReturn(Optional.of(reader));

        assertFalse(userService.isAdmin("reader"));
    }
}
