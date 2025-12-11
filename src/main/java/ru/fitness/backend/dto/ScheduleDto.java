package ru.fitness.backend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class ScheduleDto {

    private Long id;

    @NotNull(message = "Необходимо выбрать тип тренировки")
    private Long workoutTypeId;

    @NotNull(message = "Необходимо выбрать тренера")
    private Long trainerId;

    @NotNull(message = "Время начала не может быть пустым")
    @Future(message = "Время начала должно быть в будущем")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    @NotNull(message = "Количество мест не может быть пустым")
    @Min(value = 0, message = "Количество мест не может быть отрицательным")
    private Integer availableSlots;
}
