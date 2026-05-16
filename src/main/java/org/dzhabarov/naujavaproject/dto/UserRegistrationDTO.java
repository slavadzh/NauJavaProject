package org.dzhabarov.naujavaproject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Данные формы регистрации пользователя
 */
@Getter
@Setter
public class UserRegistrationDTO {

    @NotBlank(message = "Логин обязателен")
    private String name;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный email")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 4, message = "Пароль должен содержать минимум 4 символа")
    private String password;
}
