package ru.fitness.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.fitness.backend.models.Schedule;
import ru.fitness.backend.models.User;
import ru.fitness.backend.models.WorkoutType;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long>, JpaSpecificationExecutor<Schedule> {

    /**
     * Finds all schedule entries and eagerly fetches related WorkoutType and Trainer entities
     * to prevent the N+1 select problem. The results are ordered by start time.
     * @return A list of all schedules with detailed information.
     */
    @Query("SELECT s FROM Schedule s JOIN FETCH s.workoutType JOIN FETCH s.trainer ORDER BY s.startTime ASC")
    List<Schedule> findAllWithDetails();

    List<Schedule> findByTrainer(User trainer);
    // Найдёт все записи расписания для конкретного тренера

    List<Schedule> findByWorkoutType(WorkoutType workoutType);
    // Найдёт все записи для конкретного типа тренировки

    List<Schedule> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    // Найдёт записи в указанном временном диапазоне

    List<Schedule> findByAvailableSlotsGreaterThan(int slots);
    // Найдёт записи, где осталось больше N мест
}