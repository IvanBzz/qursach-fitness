package ru.fitness.backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.fitness.backend.dto.DashboardStatsDto;
import ru.fitness.backend.models.WorkoutType;
import ru.fitness.backend.repositories.UserRepository;
import ru.fitness.backend.repositories.WorkoutSubscriptionRepository;
import ru.fitness.backend.repositories.WorkoutTypeRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final WorkoutSubscriptionRepository workoutSubscriptionRepository;
    private final WorkoutTypeRepository workoutTypeRepository;

    public DashboardStatsDto getDashboardStatistics() {
        long totalUsers = userRepository.count();
        long newUsers = userRepository.countByDateOfCreatedAfter(LocalDateTime.now().minusDays(7));

        var workoutPopularity = workoutSubscriptionRepository.findWorkoutPopularity();

        List<WorkoutType> allTypes = workoutTypeRepository.findAll();
        double avgDuration = allTypes.stream()
                .mapToInt(WorkoutType::getDurationMinutes)
                .average()
                .orElse(0.0);

        return DashboardStatsDto.builder()
                .totalUsers(totalUsers)
                .newUsersLast7Days(newUsers)
                .averageWorkoutDuration(Math.round(avgDuration * 10.0) / 10.0) // Round to 1 decimal place
                .workoutPopularity(workoutPopularity)
                .build();
    }
}
