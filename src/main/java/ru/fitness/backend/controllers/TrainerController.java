package ru.fitness.backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.fitness.backend.services.ScheduleService;
import ru.fitness.backend.services.UserService;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/trainer")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_TRAINER')")
public class TrainerController {

    private final UserService userService;
    private final ScheduleService scheduleService;

    @GetMapping("/my-schedules")
    public String viewMySchedules(Model model) {
        return userService.getCurrentUser()
                .map(trainer -> {
                    model.addAttribute("schedules", scheduleService.findSchedulesByTrainer(trainer));
                    return "trainer/my-schedules";
                })
                .orElse("redirect:/login"); // Should not happen with @PreAuthorize
    }

    @GetMapping("/schedules/{scheduleId}/subscribers")
    public String viewSubscribers(@PathVariable("scheduleId") Long scheduleId, Model model) {
        return userService.getCurrentUser()
                .map(trainer -> {
                    try {
                        model.addAttribute("schedule", scheduleService.findById(scheduleId));
                        model.addAttribute("subscribers", scheduleService.findSubscribersForSchedule(scheduleId, trainer));
                        return "trainer/schedule-subscribers";
                    } catch (NoSuchElementException e) {
                        // Handle schedule not found or not owned by trainer
                        return "redirect:/trainer/my-schedules";
                    }
                })
                .orElse("redirect:/login");
    }
}
