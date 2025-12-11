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
        // Here you might want to check if there are existing schedules with this type
        // and either prevent deletion or cascade delete (though cascade is usually DB side or JPA)
        workoutTypeRepository.deleteById(id);
    }
}
