package org.dzhabarov.naujavaproject.controller;

import jakarta.validation.Valid;
import org.dzhabarov.naujavaproject.dto.UserRegistrationDTO;
import org.dzhabarov.naujavaproject.exception.BusinessException;
import org.dzhabarov.naujavaproject.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Контроллер регистрации и входа в систему (Thymeleaf)
 */
@Controller
public class AuthController {

    private final UserService userService;

    /**
     * @param userService сервис пользователей
     */
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /** Отображает форму регистрации */
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registration", new UserRegistrationDTO());
        return "register";
    }

    /** Обрабатывает регистрацию нового пользователя */
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registration") UserRegistrationDTO registration,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            userService.register(registration);
            redirectAttributes.addFlashAttribute("successMessage", "Регистрация успешна. Войдите в систему.");
            return "redirect:/login";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/register";
        }
    }

    /** Отображает форму входа */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
