package ru.fitness.backend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.fitness.backend.dto.UserRegistrationDto;
import ru.fitness.backend.exceptions.UserAlreadyExistException;
import ru.fitness.backend.services.UserService;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registration")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userDto", new UserRegistrationDto());
        return "registration";
    }

    @PostMapping("/registration")
    public String registerUser(
            @Valid @ModelAttribute("userDto") UserRegistrationDto userDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "registration";
        }

        try {
            userService.registerNewUser(userDto);
            redirectAttributes.addFlashAttribute("registrationSuccess", "Регистрация прошла успешно! Теперь вы можете войти.");
            return "redirect:/login";
        } catch (UserAlreadyExistException e) {
            model.addAttribute("registrationError", e.getMessage());
            return "registration";
        } catch (Exception e) {
            model.addAttribute("registrationError", "Произошла непредвиденная ошибка при регистрации.");
            return "registration";
        }
    }
}
