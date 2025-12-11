package ru.fitness.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.fitness.backend.models.WorkoutType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutPopularityDto {
    private WorkoutType workoutType;
    private long subscriptionCount;
}
