package ru.fitness.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.fitness.backend.dto.validation.PasswordMatches;

@Data
@PasswordMatches
public class UserRegistrationDto {

    @NotBlank(message = "Имя не может быть пустым")
    @Size(max = 255, message = "Имя не должно превышать 255 символов")
    private String fullName;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    private String email;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 8, message = "Пароль должен содержать не менее 8 символов")
    private String password;

    @NotBlank(message = "Подтверждение пароля не может быть пустым")
    private String confirmPassword;
}