package ru.fitness.backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fitness.backend.models.WorkoutType;
import ru.fitness.backend.repositories.WorkoutTypeRepository;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class WorkoutTypeService {

    private final WorkoutTypeRepository workoutTypeRepository;
    private final ru.fitness.backend.repositories.ScheduleRepository scheduleRepository;
    private final ru.fitness.backend.repositories.WorkoutSubscriptionRepository workoutSubscriptionRepository;

    public List<WorkoutType> findAll() {
        return workoutTypeRepository.findAll();
    }

    public WorkoutType findById(Long id) {
        return workoutTypeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Тип тренировки с ID " + id + " не найден"));
    }

    @Transactional
    public void createWorkoutType(WorkoutType workoutType) {
        workoutTypeRepository.save(workoutType);
    }

    @Transactional
    public void updateWorkoutType(Long id, WorkoutType updatedWorkoutType) {
        WorkoutType existing = findById(id);
        existing.setTitle(updatedWorkoutType.getTitle());
        existing.setDescription(updatedWorkoutType.getDescription());
        existing.setDurationMinutes(updatedWorkoutType.getDurationMinutes());
        workoutTypeRepository.save(existing);
    }

    @Transactional
    public void deleteWorkoutType(Long id) {
        WorkoutType workoutType = findById(id);
        
        // Find all schedules with this workout type
        List<ru.fitness.backend.models.Schedule> schedules = scheduleRepository.findByWorkoutType(workoutType);
        
        // Delete each schedule and its subscriptions
        for (ru.fitness.backend.models.Schedule schedule : schedules) {
            workoutSubscriptionRepository.deleteAllBySchedule(schedule);
            scheduleRepository.delete(schedule);
        }
        
        workoutTypeRepository.deleteById(id);
    }
}
