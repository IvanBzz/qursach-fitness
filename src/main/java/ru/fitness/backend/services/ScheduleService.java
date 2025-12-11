package ru.fitness.backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.fitness.backend.dto.ScheduleDto;
import ru.fitness.backend.exceptions.AlreadySignedUpException;
import ru.fitness.backend.exceptions.NoAvailableSlotsException;
import ru.fitness.backend.models.*;
import ru.fitness.backend.repositories.ScheduleRepository;
import ru.fitness.backend.repositories.UserRepository;
import ru.fitness.backend.repositories.WorkoutSubscriptionRepository;
import ru.fitness.backend.repositories.WorkoutTypeRepository;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;


@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final WorkoutSubscriptionRepository workoutSubscriptionRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final WorkoutTypeRepository workoutTypeRepository;

    /**
     * Creates a new workout schedule based on the provided DTO.
     * @param scheduleDto DTO containing the schedule information.
     * @throws NoSuchElementException if the trainer or workout type is not found.
     */
    @Transactional
    public void createSchedule(ScheduleDto scheduleDto) {
        WorkoutType workoutType = workoutTypeRepository.findById(scheduleDto.getWorkoutTypeId())
                .orElseThrow(() -> new NoSuchElementException("Тип тренировки с ID " + scheduleDto.getWorkoutTypeId() + " не найден."));

        User trainer = userRepository.findById(scheduleDto.getTrainerId())
                .orElseThrow(() -> new NoSuchElementException("Тренер с ID " + scheduleDto.getTrainerId() + " не найден."));
        
        // Optional: Check if the user actually has the TRAINER role
        if (!trainer.getRoles().contains(Role.ROLE_TRAINER)) {
            throw new IllegalArgumentException("Пользователь с ID " + scheduleDto.getTrainerId() + " не является тренером.");
        }

        Schedule schedule = new Schedule();
        schedule.setWorkoutType(workoutType);
        schedule.setTrainer(trainer);
        schedule.setStartTime(scheduleDto.getStartTime());
        schedule.setAvailableSlots(scheduleDto.getAvailableSlots());

        scheduleRepository.save(schedule);
    }
    
    /**
     * Finds all schedule entries using an optimized query to prevent N+1 issues.
     * @return A list of all schedules with detailed information, sorted by start time.
     */
    public List<Schedule> findAllSchedules() {
        return scheduleRepository.findAllWithDetails();
    }

    /**
     * Finds schedules based on keyword, sort field, and sort direction.
     * @param keyword Optional keyword for searching by workout title or trainer full name.
     * @param sortField Optional field to sort by (e.g., "workoutType.title", "trainer.fullName", "startTime").
     * @param sortDir Optional sort direction ("asc" or "desc").
     * @return A list of schedules.
     */
    public List<Schedule> findSchedules(String keyword, String sortField, String sortDir) {
        Sort sort = Sort.by(sortField != null && !sortField.isEmpty() ? sortField : "startTime");
        if (sortDir != null && sortDir.equals("desc")) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }

        Specification<Schedule> spec = (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.join("workoutType").get("title")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.join("trainer").get("fullName")), likeKeyword)
                ));
            }
            return predicate;
        };

        return scheduleRepository.findAll(spec, sort);
    }

    /**
     * Finds all schedules associated with a specific trainer.
     * @param trainer The User object representing the trainer.
     * @return A list of schedules conducted by the given trainer.
     */
    public List<Schedule> findSchedulesByTrainer(User trainer) {
        return scheduleRepository.findByTrainer(trainer);
    }

    /**
     * Finds all subscribers for a specific schedule, ensuring the schedule belongs to the given trainer.
     * @param scheduleId The ID of the schedule.
     * @param trainer The User object representing the trainer who owns the schedule.
     * @return A list of Users subscribed to the schedule.
     * @throws NoSuchElementException if the schedule is not found.
     * @throws IllegalArgumentException if the schedule does not belong to the provided trainer.
     */
    public List<User> findSubscribersForSchedule(Long scheduleId, User trainer) {
        Schedule schedule = findById(scheduleId);
        if (!schedule.getTrainer().equals(trainer)) {
            throw new IllegalArgumentException("Тренировка с ID " + scheduleId + " не принадлежит текущему тренеру.");
        }
        return workoutSubscriptionRepository.findAllBySchedule(schedule).stream()
                .map(WorkoutSubscription::getUser)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Finds a single schedule entry by its ID.
     * @param id The ID of the schedule.
     * @return The Schedule object.
     * @throws NoSuchElementException if no schedule is found with the given ID.
     */
    public Schedule findById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Тренировка с ID " + id + " не найдена"));
    }

    /**
     * Subscribes the current user to a workout schedule.
     * @param scheduleId The ID of the schedule to subscribe to.
     * @throws NoAvailableSlotsException if there are no slots available.
     * @throws AlreadySignedUpException if the user is already signed up.
     * @throws NoSuchElementException if the user or schedule cannot be found.
     */
    @Transactional
    public void signUpForWorkout(Long scheduleId) throws NoAvailableSlotsException, AlreadySignedUpException {
        User currentUser = userService.getCurrentUser()
                .orElseThrow(() -> new NoSuchElementException("Не удалось определить текущего пользователя."));
        
        Schedule schedule = findById(scheduleId);

        if (schedule.getTrainer().equals(currentUser)) {
            throw new IllegalArgumentException("Вы не можете записаться на собственную тренировку.");
        }

        if (isUserSubscribed(currentUser, schedule)) {
            throw new AlreadySignedUpException("Вы уже записаны на эту тренировку.");
        }

        if (schedule.getAvailableSlots() <= 0) {
            throw new NoAvailableSlotsException("На эту тренировку нет свободных мест.");
        }

        // All checks passed, proceed with subscription
        schedule.setAvailableSlots(schedule.getAvailableSlots() - 1);
        scheduleRepository.save(schedule);

        WorkoutSubscription subscription = new WorkoutSubscription(currentUser, schedule);
        workoutSubscriptionRepository.save(subscription);
    }

    /**
     * Finds all workout subscriptions for the currently authenticated user.
     * An optimized query should be added later to fetch details eagerly.
     * @return A list of WorkoutSubscription objects.
     */
    public List<WorkoutSubscription> findSubscriptionsForCurrentUser() {
        return userService.getCurrentUser()
                .map(workoutSubscriptionRepository::findByUser)
                .orElse(List.of());
    }

    /**
     * Cancels a user's subscription to a workout.
     * @param subscriptionId The ID of the subscription to cancel.
     * @throws NoSuchElementException if the subscription is not found.
     * @throws IllegalStateException if the subscription does not belong to the current user.
     */
    @Transactional
    public void cancelSubscription(Long subscriptionId) {
        User currentUser = userService.getCurrentUser()
                .orElseThrow(() -> new NoSuchElementException("Не удалось определить текущего пользователя."));

        WorkoutSubscription subscription = workoutSubscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new NoSuchElementException("Запись с ID " + subscriptionId + " не найдена."));

        if (!subscription.getUser().equals(currentUser)) {
            throw new IllegalStateException("Вы не можете отменить чужую запись.");
        }

        // All checks passed, proceed with cancellation
        Schedule schedule = subscription.getSchedule();
        schedule.setAvailableSlots(schedule.getAvailableSlots() + 1);
        scheduleRepository.save(schedule);

        workoutSubscriptionRepository.delete(subscription);
    }

    /**
     * Updates an existing workout schedule.
     * @param scheduleId The ID of the schedule to update.
     * @param scheduleDto DTO containing the new schedule information.
     * @throws NoSuchElementException if the schedule, trainer, or workout type is not found.
     */
    @Transactional
    public void updateSchedule(Long scheduleId, ScheduleDto scheduleDto) {
        Schedule schedule = findById(scheduleId);

        WorkoutType workoutType = workoutTypeRepository.findById(scheduleDto.getWorkoutTypeId())
                .orElseThrow(() -> new NoSuchElementException("Тип тренировки с ID " + scheduleDto.getWorkoutTypeId() + " не найден."));

        User trainer = userRepository.findById(scheduleDto.getTrainerId())
                .orElseThrow(() -> new NoSuchElementException("Тренер с ID " + scheduleDto.getTrainerId() + " не найден."));

        if (!trainer.getRoles().contains(Role.ROLE_TRAINER)) {
            throw new IllegalArgumentException("Пользователь с ID " + scheduleDto.getTrainerId() + " не является тренером.");
        }

        schedule.setWorkoutType(workoutType);
        schedule.setTrainer(trainer);
        schedule.setStartTime(scheduleDto.getStartTime());
        schedule.setAvailableSlots(scheduleDto.getAvailableSlots());

        scheduleRepository.save(schedule);
    }
    
    /**
     * Deletes a schedule entry and all its related subscriptions.
     * @param scheduleId The ID of the schedule to delete.
     */
    @Transactional
    public void deleteSchedule(Long scheduleId) {
        Schedule schedule = findById(scheduleId);
        // First, delete all subscriptions to this schedule to avoid constraint violations
        workoutSubscriptionRepository.deleteAllBySchedule(schedule);
        // Then, delete the schedule itself
        scheduleRepository.delete(schedule);
    }

    /**
     * Finds the next upcoming workout for the current user.
     * @return An Optional containing the next workout subscription, or empty if none is found.
     */
    public java.util.Optional<WorkoutSubscription> findNextUpcomingSubscription() {
        return userService.getCurrentUser()
                .flatMap(user -> workoutSubscriptionRepository.findFirstByUserAndScheduleStartTimeAfterOrderByScheduleStartTimeAsc(user, java.time.LocalDateTime.now()));
    }

    /**
     * Checks if a user is subscribed to a specific schedule.
     * @param user The user to check.
     * @param schedule The schedule to check.
     * @return true if the user is subscribed, false otherwise.
     */
    public boolean isUserSubscribed(User user, Schedule schedule) {
        return workoutSubscriptionRepository.existsByUserAndSchedule(user, schedule);
    }
}