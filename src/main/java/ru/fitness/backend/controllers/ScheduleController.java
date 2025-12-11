package ru.fitness.backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.fitness.backend.exceptions.AlreadySignedUpException;
import ru.fitness.backend.exceptions.NoAvailableSlotsException;
import ru.fitness.backend.services.ScheduleService;
import ru.fitness.backend.services.UserService;

import java.util.NoSuchElementException;

@Controller
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final UserService userService;

    @GetMapping("/schedule")
    public String showSchedule(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "sortField", defaultValue = "startTime") String sortField,
                               @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
                               Model model) {
        model.addAttribute("schedules", scheduleService.findSchedules(keyword, sortField, sortDir));
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        userService.getCurrentUser().ifPresent(user -> model.addAttribute("currentUser", user));
        return "schedule";
    }

    @PostMapping("/schedule/signup/{id}")
    public String signUpForWorkout(@PathVariable("id") Long scheduleId, RedirectAttributes redirectAttributes) {
        try {
            scheduleService.signUpForWorkout(scheduleId);
            redirectAttributes.addFlashAttribute("successMessage", "Вы успешно записались на тренировку!");
        } catch (NoAvailableSlotsException | AlreadySignedUpException | NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Произошла непредвиденная ошибка. Попробуйте снова.");
        }
        return "redirect:/schedule";
    }
}