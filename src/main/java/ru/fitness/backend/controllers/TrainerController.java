package ru.fitness.backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.fitness.backend.services.ScheduleService;
import ru.fitness.backend.services.UserService;
import ru.fitness.backend.repositories.WorkoutTypeRepository;

import java.time.LocalDate;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/trainer")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_TRAINER')")
public class TrainerController {

    private final UserService userService;
    private final ScheduleService scheduleService;
    private final WorkoutTypeRepository workoutTypeRepository;

    @GetMapping("/my-schedules")
    public String viewMySchedules(@RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                  @RequestParam(value = "workoutTypeId", required = false) Long workoutTypeId,
                                  Model model) {
        return userService.getCurrentUser()
                .map(trainer -> {
                    java.util.List<ru.fitness.backend.models.Schedule> allSchedules = scheduleService.findSchedulesByTrainer(trainer, date, workoutTypeId);
                    java.time.LocalDateTime now = java.time.LocalDateTime.now();
                    
                    // Разделяем на активные (будущие) и прошедшие
                    java.util.List<ru.fitness.backend.models.Schedule> activeSchedules = allSchedules.stream()
                            .filter(s -> s.getStartTime().isAfter(now))
                            .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                            .toList();
                    
                    java.util.List<ru.fitness.backend.models.Schedule> historySchedules = allSchedules.stream()
                            .filter(s -> s.getStartTime().isBefore(now))
                            .sorted((a, b) -> b.getStartTime().compareTo(a.getStartTime()))
                            .toList();
                    
                    model.addAttribute("activeSchedules", activeSchedules);
                    model.addAttribute("historySchedules", historySchedules);
                    model.addAttribute("selectedDate", date);
                    model.addAttribute("selectedWorkoutTypeId", workoutTypeId);
                    model.addAttribute("workoutTypes", workoutTypeRepository.findAll());
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
