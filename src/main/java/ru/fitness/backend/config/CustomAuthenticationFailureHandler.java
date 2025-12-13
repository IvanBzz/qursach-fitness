package ru.fitness.backend.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static org.springframework.security.web.WebAttributes.AUTHENTICATION_EXCEPTION;

@Component
@Slf4j
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.error("Authentication failed: {} - {}", exception.getClass().getName(), exception.getMessage());
        
        String errorMessage = "Неверный логин или пароль";

        if (exception instanceof org.springframework.security.authentication.DisabledException) {
            errorMessage = "Ваш аккаунт заблокирован";
        } else if (exception instanceof org.springframework.security.authentication.LockedException) {
            errorMessage = "Ваш аккаунт заблокирован";
        }
        
        request.getSession().setAttribute("errorMessage", errorMessage);
        
        response.sendRedirect("/login?error");
    }
}
