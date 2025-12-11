package ru.fitness.backend.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.fitness.backend.dto.UserRegistrationDto;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, UserRegistrationDto> {

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
    }

    @Override
    public boolean isValid(UserRegistrationDto userDto, ConstraintValidatorContext context) {
        if (userDto.getPassword() == null || userDto.getConfirmPassword() == null) {
            return false;
        }
        
        boolean isValid = userDto.getPassword().equals(userDto.getConfirmPassword());
        
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
        }
        
        return isValid;
    }
}
