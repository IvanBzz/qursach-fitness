package ru.fitness.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.fitness.backend.dto.WorkoutPopularityDto;
import ru.fitness.backend.models.Schedule;
import ru.fitness.backend.models.User;
import ru.fitness.backend.models.WorkoutSubscription;

import java.util.List;
import java.util.Optional;

public interface WorkoutSubscriptionRepository extends JpaRepository<WorkoutSubscription, Long> {

    boolean existsByUserAndSchedule(User user, Schedule schedule);

    List<WorkoutSubscription> findByUser(User user);

    Optional<WorkoutSubscription> findByUserAndSchedule(User user, Schedule schedule);

    void deleteAllBySchedule(Schedule schedule);

    List<WorkoutSubscription> findAllBySchedule(Schedule schedule); // Добавлен метод

    Optional<WorkoutSubscription> findFirstByUserAndScheduleStartTimeAfterOrderByScheduleStartTimeAsc(User user, java.time.LocalDateTime now);

    @Query("SELECT new ru.fitness.backend.dto.WorkoutPopularityDto(s.workoutType, COUNT(ws.id)) " +
           "FROM WorkoutSubscription ws JOIN ws.schedule s " +
           "GROUP BY s.workoutType " +
           "ORDER BY COUNT(ws.id) DESC")
    List<WorkoutPopularityDto> findWorkoutPopularity();
}
