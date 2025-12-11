package ru.fitness.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.fitness.backend.models.WorkoutType;
import java.util.List;

public interface WorkoutTypeRepository extends JpaRepository<WorkoutType, Long> {
    List<WorkoutType> findByTitle(String title);
    // Найдёт все тренировки с определённым названием

    List<WorkoutType> findByDurationMinutes(int duration);
    // Найдёт тренировки по продолжительности
}