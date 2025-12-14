package ru.fitness.backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.TransactionSystemException;
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
import ru.fitness.backend.services.WorkoutTypeService;

import java.time.LocalDate;
import java.util.NoSuchElementException;

@Controller
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final UserService userService;
    private final WorkoutTypeService workoutTypeService;

    @GetMapping("/schedule")
    public String showSchedule(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                               @RequestParam(value = "workoutTypeId", required = false) Long workoutTypeId,
                               @RequestParam(value = "sortField", defaultValue = "startTime") String sortField,
                               @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
                               Model model) {
        java.util.List<ru.fitness.backend.models.Schedule> allSchedules = scheduleService.findSchedules(keyword, date, workoutTypeId, sortField, sortDir);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        
        // Разделяем на актуальные (будущие) и прошедшие
        java.util.List<ru.fitness.backend.models.Schedule> activeSchedules = allSchedules.stream()
                .filter(s -> s.getStartTime().isAfter(now))
                .collect(java.util.stream.Collectors.toList());
        
        java.util.List<ru.fitness.backend.models.Schedule> pastSchedules = allSchedules.stream()
                .filter(s -> s.getStartTime().isBefore(now))
                .collect(java.util.stream.Collectors.toList());
        
        // Применяем дополнительную сортировку только если выбрана сортировка по времени (по умолчанию)
        // Для других сортировок (по местам, по названию и т.д.) используем уже отсортированный список
        if ("startTime".equals(sortField) || sortField == null || sortField.isEmpty()) {
            // Для сортировки по времени: сначала со свободными местами, потом без мест, затем по времени
            activeSchedules = activeSchedules.stream()
                    .sorted((a, b) -> {
                        // Сначала со свободными местами, потом без мест
                        if (a.getAvailableSlots() > 0 && b.getAvailableSlots() == 0) return -1;
                        if (a.getAvailableSlots() == 0 && b.getAvailableSlots() > 0) return 1;
                        // Если одинаково, сортируем по времени
                        return a.getStartTime().compareTo(b.getStartTime());
                    })
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // Прошедшие всегда сортируем по убыванию времени (самые свежие сверху)
        pastSchedules = pastSchedules.stream()
                .sorted((a, b) -> b.getStartTime().compareTo(a.getStartTime()))
                .collect(java.util.stream.Collectors.toList());
        
        model.addAttribute("activeSchedules", activeSchedules);
        model.addAttribute("pastSchedules", pastSchedules);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedWorkoutTypeId", workoutTypeId);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("workoutTypes", workoutTypeService.findAll());
        userService.getCurrentUser().ifPresent(user -> {
            model.addAttribute("currentUser", user);
            // Получаем список ID тренировок, на которые пользователь уже записан
            java.util.List<Long> subscribedIds = scheduleService.findSubscriptionsForCurrentUser().stream()
                    .map(sub -> sub.getSchedule().getId())
                    .toList();
            model.addAttribute("subscribedScheduleIds", subscribedIds);
        });
        return "schedule";
    }

    @PostMapping("/schedule/signup/{id}")
    public String signUpForWorkout(@PathVariable("id") Long scheduleId, RedirectAttributes redirectAttributes) {
        try {
            scheduleService.signUpForWorkout(scheduleId);
            redirectAttributes.addFlashAttribute("successMessage", "Вы успешно записались на тренировку!");
        } catch (NoAvailableSlotsException | AlreadySignedUpException | NoSuchElementException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (TransactionSystemException | JpaSystemException e) {
            // Обрабатываем ошибки транзакций и валидации
            Throwable rootCause = e.getRootCause();
            if (rootCause != null && rootCause.getMessage() != null) {
                String message = rootCause.getMessage();
                if (message.contains("Время начала должно быть в будущем")) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Нельзя записаться на тренировку, которая уже началась.");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Произошла ошибка при записи. Попробуйте снова.");
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Произошла ошибка при записи. Попробуйте снова.");
            }
            e.printStackTrace(); // Для отладки
        } catch (RuntimeException e) {
            // Обрабатываем RuntimeException, который может содержать более детальное сообщение
            String message = e.getMessage();
            if (message != null && message.contains("Произошла ошибка")) {
                redirectAttributes.addFlashAttribute("errorMessage", message);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Произошла непредвиденная ошибка. Попробуйте снова.");
            }
            e.printStackTrace(); // Для отладки
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Произошла непредвиденная ошибка. Попробуйте снова.");
            e.printStackTrace(); // Для отладки
        }
        return "redirect:/schedule";
    }
}
