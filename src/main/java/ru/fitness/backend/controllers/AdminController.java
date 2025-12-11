package ru.fitness.backend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.fitness.backend.dto.ScheduleDto;
import ru.fitness.backend.models.Role;
import ru.fitness.backend.models.WorkoutType;
import ru.fitness.backend.repositories.UserRepository;
import ru.fitness.backend.services.DashboardService;
import ru.fitness.backend.services.ScheduleService;
import ru.fitness.backend.services.UserService;
import ru.fitness.backend.services.WorkoutTypeService;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')") // All methods in this controller require ADMIN role
public class AdminController {

    private final ScheduleService scheduleService;
    private final UserRepository userRepository;
    private final WorkoutTypeService workoutTypeService;
    private final UserService userService;
    private final DashboardService dashboardService;
    private final ru.fitness.backend.services.NewsService newsService;

    // --- News Management ---
    @GetMapping("/news")
    public String listNews(Model model) {
        model.addAttribute("newsList", newsService.getAllNews());
        return "admin/news";
    }

    @PostMapping("/news/new")
    public String createNews(@RequestParam String title, @RequestParam String content, RedirectAttributes redirectAttributes) {
        try {
            newsService.createNews(title, content);
            redirectAttributes.addFlashAttribute("successMessage", "Новость успешно опубликована.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при создании новости: " + e.getMessage());
        }
        return "redirect:/admin/news";
    }

    @PostMapping("/news/delete/{id}")
    public String deleteNews(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            newsService.deleteNews(id);
            redirectAttributes.addFlashAttribute("successMessage", "Новость удалена.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении: " + e.getMessage());
        }
        return "redirect:/admin/news";
    }

    // --- User Management ---

    @GetMapping("/users")
    public String listUsers(@RequestParam(value = "keyword", required = false) String keyword,
                            @RequestParam(value = "sortField", defaultValue = "id") String sortField,
                            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
                            Model model) {
        model.addAttribute("users", userService.findUsers(keyword, sortField, sortDir));
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        return "admin/users";
    }

    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable("id") Long userId, Model model) {
        try {
            model.addAttribute("user", userService.findById(userId));
            model.addAttribute("allRoles", Role.values());
            return "admin/edit-user";
        } catch (Exception e) {
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/users/edit/{id}")
    public String updateUserRoles(@PathVariable("id") Long userId,
                                  @RequestParam(value = "roles", required = false) java.util.HashSet<Role> roles,
                                  RedirectAttributes redirectAttributes) {
        try {
            // Ensure roles are not null if no checkboxes are selected
            if (roles == null) {
                roles = new java.util.HashSet<>();
            }
            // A user must have at least ROLE_USER
            roles.add(Role.ROLE_USER);

            userService.updateUserRoles(userId, roles);
            redirectAttributes.addFlashAttribute("successMessage", "Роли пользователя успешно обновлены.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении ролей: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // --- Dashboard ---

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("stats", dashboardService.getDashboardStatistics());
        return "admin/dashboard";
    }

    // --- Schedule Management ---

    @GetMapping("/schedule/new")
    public String showCreateScheduleForm(Model model) {
        model.addAttribute("scheduleDto", new ScheduleDto());
        model.addAttribute("allTrainers", userRepository.findAllByRolesContaining(Role.ROLE_TRAINER));
        model.addAttribute("allWorkoutTypes", workoutTypeService.findAll());
        return "admin/create-schedule";
    }

    @PostMapping("/schedule/new")
    public String createSchedule(@Valid @ModelAttribute("scheduleDto") ScheduleDto scheduleDto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("allTrainers", userRepository.findAllByRolesContaining(Role.ROLE_TRAINER));
            model.addAttribute("allWorkoutTypes", workoutTypeService.findAll());
            return "admin/create-schedule";
        }

        try {
            scheduleService.createSchedule(scheduleDto);
            redirectAttributes.addFlashAttribute("successMessage", "Новая тренировка успешно добавлена в расписание.");
            return "redirect:/schedule";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при создании тренировки: " + e.getMessage());
            return "redirect:/admin/schedule/new";
        }
    }

    @GetMapping("/schedule/edit/{id}")
    public String showEditScheduleForm(@PathVariable("id") Long scheduleId, Model model) {
        try {
            ru.fitness.backend.models.Schedule schedule = scheduleService.findById(scheduleId);
            ScheduleDto dto = new ScheduleDto();
            dto.setId(schedule.getId());
            dto.setTrainerId(schedule.getTrainer().getId());
            dto.setWorkoutTypeId(schedule.getWorkoutType().getId());
            dto.setStartTime(schedule.getStartTime());
            dto.setAvailableSlots(schedule.getAvailableSlots());

            model.addAttribute("scheduleDto", dto);
            model.addAttribute("allTrainers", userRepository.findAllByRolesContaining(Role.ROLE_TRAINER));
            model.addAttribute("allWorkoutTypes", workoutTypeService.findAll());
            return "admin/edit-schedule";
        } catch (Exception e) {
            return "redirect:/schedule";
        }
    }

    @PostMapping("/schedule/edit/{id}")
    public String updateSchedule(@PathVariable("id") Long scheduleId,
                                 @Valid @ModelAttribute("scheduleDto") ScheduleDto scheduleDto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("allTrainers", userRepository.findAllByRolesContaining(Role.ROLE_TRAINER));
            model.addAttribute("allWorkoutTypes", workoutTypeService.findAll());
            return "admin/edit-schedule";
        }

        try {
            scheduleService.updateSchedule(scheduleId, scheduleDto);
            redirectAttributes.addFlashAttribute("successMessage", "Тренировка успешно обновлена.");
            return "redirect:/schedule";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении тренировки: " + e.getMessage());
            return "redirect:/admin/schedule/edit/" + scheduleId;
        }
    }

    @PostMapping("/schedule/delete/{id}")
    public String deleteSchedule(@PathVariable("id") Long scheduleId, RedirectAttributes redirectAttributes) {
        try {
            scheduleService.deleteSchedule(scheduleId);
            redirectAttributes.addFlashAttribute("successMessage", "Тренировка успешно удалена.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении тренировки: " + e.getMessage());
        }
        return "redirect:/schedule";
    }

    // --- Workout Type Management ---

    @GetMapping("/workout-types")
    public String listWorkoutTypes(Model model) {
        model.addAttribute("workoutTypes", workoutTypeService.findAll());
        return "admin/workout-types";
    }

    @GetMapping("/workout-types/new")
    public String showCreateWorkoutTypeForm(Model model) {
        model.addAttribute("workoutType", new WorkoutType());
        return "admin/create-workout-type";
    }

    @PostMapping("/workout-types/new")
    public String createWorkoutType(@Valid @ModelAttribute("workoutType") WorkoutType workoutType,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/create-workout-type";
        }
        try {
            workoutTypeService.createWorkoutType(workoutType);
            redirectAttributes.addFlashAttribute("successMessage", "Тип тренировки успешно создан.");
            return "redirect:/admin/workout-types";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
            return "admin/create-workout-type";
        }
    }

    @GetMapping("/workout-types/edit/{id}")
    public String showEditWorkoutTypeForm(@PathVariable("id") Long id, Model model) {
        try {
            model.addAttribute("workoutType", workoutTypeService.findById(id));
            return "admin/edit-workout-type";
        } catch (Exception e) {
            return "redirect:/admin/workout-types";
        }
    }

    @PostMapping("/workout-types/edit/{id}")
    public String updateWorkoutType(@PathVariable("id") Long id,
                                    @Valid @ModelAttribute("workoutType") WorkoutType workoutType,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/edit-workout-type";
        }
        try {
            workoutTypeService.updateWorkoutType(id, workoutType);
            redirectAttributes.addFlashAttribute("successMessage", "Тип тренировки успешно обновлен.");
            return "redirect:/admin/workout-types";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
            return "admin/edit-workout-type";
        }
    }

    @PostMapping("/workout-types/delete/{id}")
    public String deleteWorkoutType(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            workoutTypeService.deleteWorkoutType(id);
            redirectAttributes.addFlashAttribute("successMessage", "Тип тренировки успешно удален.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
        }
        return "redirect:/admin/workout-types";
    }
}
