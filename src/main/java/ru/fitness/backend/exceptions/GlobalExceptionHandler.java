package ru.fitness.backend.exceptions;

import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ModelAndView handleNoSuchElementException(NoSuchElementException ex) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorMessage", ex.getMessage());
        mav.addObject("statusCode", 404);
        return mav;
    }

    @ExceptionHandler({InvalidCsrfTokenException.class, MissingCsrfTokenException.class})
    public ModelAndView handleCsrfException(Exception ex) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorMessage", "Ошибка безопасности. Пожалуйста, попробуйте снова.");
        mav.addObject("statusCode", 403);
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGenericException(Exception ex) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorMessage", "Произошла непредвиденная ошибка: " + ex.getMessage());
        mav.addObject("statusCode", 500);
        return mav;
    }
}
