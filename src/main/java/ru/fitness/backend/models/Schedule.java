package ru.fitness.backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", nullable = false)
    @NotNull(message = "Тип тренировки не может быть пустым")
    private WorkoutType workoutType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    @NotNull(message = "Тренер не может быть пустым")
    private User trainer; // Ссылка на тренера (User с ролью TRAINER)

    @NotNull(message = "Время начала не может быть пустым")
    @Future(message = "Время начала должно быть в будущем")
    private LocalDateTime startTime;

    @Min(value = 0, message = "Количество мест не может быть отрицательным")
    private int availableSlots; // Сколько мест осталось

    @Min(value = 0, message = "Общее количество мест не может быть отрицательным")
    private Integer totalSlots; // Изначальное количество мест
}
