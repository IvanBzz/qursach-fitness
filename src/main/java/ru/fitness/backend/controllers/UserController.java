package ru.fitness.backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.fitness.backend.services.ScheduleService;
import ru.fitness.backend.services.UserService;

import java.util.NoSuchElementException;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final ScheduleService scheduleService;
    private final UserService userService;
    private final ru.fitness.backend.services.NewsService newsService;
    private final ru.fitness.backend.repositories.WorkoutTypeRepository workoutTypeRepository;

    @GetMapping("/home")
    public String home(Model model) {
        userService.getCurrentUser().ifPresent(user -> {
            model.addAttribute("userName", user.getFullName());
            scheduleService.findNextUpcomingSubscription().ifPresent(
                subscription -> model.addAttribute("nextWorkout", subscription)
            );
        });
        
        // Add news to the model
        model.addAttribute("newsList", newsService.getLatestNews(3));
        // Add popular workout types (just random or first 3 for now as "Featured")
        model.addAttribute("featuredWorkouts", workoutTypeRepository.findAll().stream().limit(3).toList());
        
        return "home";
    }

    @GetMapping("/about")
    public String aboutPage() {
        return "about";
    }

    @GetMapping("/my-workouts")
    public String myWorkouts(@RequestParam(value = "date", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date,
                             @RequestParam(value = "workoutTypeId", required = false) Long workoutTypeId,
                             Model model) {
        java.util.List<ru.fitness.backend.models.WorkoutSubscription> allSubs = scheduleService.findSubscriptionsForCurrentUser(date, workoutTypeId);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        
        java.util.List<ru.fitness.backend.models.WorkoutSubscription> active = allSubs.stream()
                .filter(sub -> sub.getSchedule().getStartTime().isAfter(now))
                .toList();
        
        java.util.List<ru.fitness.backend.models.WorkoutSubscription> history = allSubs.stream()
                .filter(sub -> sub.getSchedule().getStartTime().isBefore(now))
                .toList();
        
        model.addAttribute("activeSubscriptions", active);
        model.addAttribute("historySubscriptions", history);
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedWorkoutTypeId", workoutTypeId);
        model.addAttribute("workoutTypes", workoutTypeRepository.findAll());
        return "my-workouts";
    }

    @PostMapping("/my-workouts/cancel/{id}")
    public String cancelWorkout(@PathVariable("id") Long subscriptionId, RedirectAttributes redirectAttributes) {
        try {
            scheduleService.cancelSubscription(subscriptionId);
            redirectAttributes.addFlashAttribute("successMessage", "Вы успешно отменили запись на тренировку.");
        } catch (NoSuchElementException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Произошла непредвиденная ошибка. Попробуйте снова.");
        }
        return "redirect:/my-workouts";
    }

    @GetMapping("/profile")
    public String viewProfile(Model model) {
        userService.getCurrentUser().ifPresentOrElse(
                user -> model.addAttribute("user", user),
                () -> { throw new NoSuchElementException("Пользователь не найден"); }
        );
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String editProfile(Model model) {
        userService.getCurrentUser().ifPresent(
                user -> model.addAttribute("user", user)
        );
        return "edit-profile";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@RequestParam("fullName") String fullName,
                                @RequestParam("phoneNumber") String phoneNumber,
                                @RequestParam("bio") String bio,
                                RedirectAttributes redirectAttributes) {
        userService.getCurrentUser().ifPresent(user -> {
            userService.updateUserProfile(user.getId(), fullName, phoneNumber, bio);
            redirectAttributes.addFlashAttribute("successMessage", "Профиль успешно обновлен.");
        });
        return "redirect:/profile";
    }

    @GetMapping("/trainers")
    public String listTrainers(Model model) {
        model.addAttribute("trainers", userService.findTrainers());
        return "trainers";
    }
}
