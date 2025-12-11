package ru.fitness.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardStatsDto {
    private long totalUsers;
    private long newUsersLast7Days;
    private long upcomingWorkouts; // We will implement this later
    private double averageWorkoutDuration;
    private List<WorkoutPopularityDto> workoutPopularity;
    private Map<String, Long> trainerWorkoutCounts; // And this
}
