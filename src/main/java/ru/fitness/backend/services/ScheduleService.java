package ru.fitness.backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final WorkoutSubscriptionRepository workoutSubscriptionRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final WorkoutTypeRepository workoutTypeRepository;

    @Transactional
    public void createSchedule(ScheduleDto scheduleDto) {
        WorkoutType workoutType = workoutTypeRepository.findById(scheduleDto.getWorkoutTypeId())
                .orElseThrow(() -> new NoSuchElementException("Тип тренировки с ID " + scheduleDto.getWorkoutTypeId() + " не найден."));

        User trainer = userRepository.findById(scheduleDto.getTrainerId())
                .orElseThrow(() -> new NoSuchElementException("Тренер с ID " + scheduleDto.getTrainerId() + " не найден."));
        
        if (!trainer.getRoles().contains(Role.ROLE_TRAINER)) {
            throw new IllegalArgumentException("Пользователь с ID " + scheduleDto.getTrainerId() + " не является тренером.");
        }

        Schedule schedule = new Schedule();
        schedule.setWorkoutType(workoutType);
        schedule.setTrainer(trainer);
        schedule.setStartTime(scheduleDto.getStartTime());
        schedule.setAvailableSlots(scheduleDto.getAvailableSlots());
        schedule.setTotalSlots(scheduleDto.getAvailableSlots()); // Set initial capacity

        scheduleRepository.save(schedule);
    }
    
    public List<Schedule> findAllSchedules() {
        return scheduleRepository.findAllWithDetails();
    }

    public List<Schedule> findSchedules(String keyword, LocalDate date, String sortField, String sortDir) {
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

            if (date != null) {
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.between(root.get("startTime"), startOfDay, endOfDay));
            }

            return predicate;
        };

        return scheduleRepository.findAll(spec, sort);
    }

    public List<Schedule> findSchedulesByTrainer(User trainer) {
        return scheduleRepository.findByTrainer(trainer);
    }

    public List<User> findSubscribersForSchedule(Long scheduleId, User trainer) {
        Schedule schedule = findById(scheduleId);
        if (!schedule.getTrainer().equals(trainer)) {
            throw new IllegalArgumentException("Тренировка с ID " + scheduleId + " не принадлежит текущему тренеру.");
        }
        return workoutSubscriptionRepository.findAllBySchedule(schedule).stream()
                .map(WorkoutSubscription::getUser)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<User> findSubscribersForScheduleAdmin(Long scheduleId) {
        Schedule schedule = findById(scheduleId);
        return workoutSubscriptionRepository.findAllBySchedule(schedule).stream()
                .map(WorkoutSubscription::getUser)
                .collect(java.util.stream.Collectors.toList());
    }

    public Schedule findById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Тренировка с ID " + id + " не найдена"));
    }

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

        schedule.setAvailableSlots(schedule.getAvailableSlots() - 1);
        scheduleRepository.save(schedule);

        WorkoutSubscription subscription = new WorkoutSubscription(currentUser, schedule);
        workoutSubscriptionRepository.save(subscription);
    }

    public List<WorkoutSubscription> findSubscriptionsForCurrentUser() {
        return userService.getCurrentUser()
                .map(workoutSubscriptionRepository::findByUser)
                .orElse(List.of());
    }

    @Transactional
    public void cancelSubscription(Long subscriptionId) {
        User currentUser = userService.getCurrentUser()
                .orElseThrow(() -> new NoSuchElementException("Не удалось определить текущего пользователя."));

        WorkoutSubscription subscription = workoutSubscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new NoSuchElementException("Запись с ID " + subscriptionId + " не найдена."));

        if (!subscription.getUser().equals(currentUser)) {
            throw new IllegalStateException("Вы не можете отменить чужую запись.");
        }

        Schedule schedule = subscription.getSchedule();
        schedule.setAvailableSlots(schedule.getAvailableSlots() + 1);
        scheduleRepository.save(schedule);

        workoutSubscriptionRepository.delete(subscription);
    }

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

        int bookedSlots = 0;
        if (schedule.getTotalSlots() != null && schedule.getTotalSlots() > 0) {
             bookedSlots = schedule.getTotalSlots() - schedule.getAvailableSlots();
        }

        schedule.setWorkoutType(workoutType);
        schedule.setTrainer(trainer);
        schedule.setStartTime(scheduleDto.getStartTime());
        
        // Update capacity logic
        int newTotal = scheduleDto.getAvailableSlots(); // Input from form is treated as Total Capacity
        schedule.setTotalSlots(newTotal);
        schedule.setAvailableSlots(Math.max(0, newTotal - bookedSlots)); // Recalculate available

        scheduleRepository.save(schedule);
    }

    @Transactional
    public void deleteSchedule(Long scheduleId) {
        Schedule schedule = findById(scheduleId);
        workoutSubscriptionRepository.deleteAllBySchedule(schedule);
        scheduleRepository.delete(schedule);
    }
    
    @Transactional
    public void adminCancelSubscription(Long subscriptionId) {
        WorkoutSubscription subscription = workoutSubscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new NoSuchElementException("Запись с ID " + subscriptionId + " не найдена."));

        Schedule schedule = subscription.getSchedule();
        schedule.setAvailableSlots(schedule.getAvailableSlots() + 1);
        scheduleRepository.save(schedule);

        workoutSubscriptionRepository.delete(subscription);
    }

    public List<WorkoutSubscription> findSubscriptionsForUser(User user) {
        return workoutSubscriptionRepository.findByUser(user);
    }

    public java.util.Optional<WorkoutSubscription> findNextUpcomingSubscription() {
        return userService.getCurrentUser()
                .flatMap(user -> workoutSubscriptionRepository.findFirstByUserAndScheduleStartTimeAfterOrderByScheduleStartTimeAsc(user, java.time.LocalDateTime.now()));
    }

    public boolean isUserSubscribed(User user, Schedule schedule) {
        return workoutSubscriptionRepository.existsByUserAndSchedule(user, schedule);
    }
}
