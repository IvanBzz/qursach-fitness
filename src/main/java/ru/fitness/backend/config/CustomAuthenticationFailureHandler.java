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
        
        // Save the exception in session for login.html to retrieve details
        request.getSession().setAttribute(AUTHENTICATION_EXCEPTION, exception);
        
        response.sendRedirect("/login?error");
    }
}
