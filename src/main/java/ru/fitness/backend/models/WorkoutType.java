package ru.fitness.backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
public class WorkoutType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    @Size(max = 100, message = "Название должно быть не длиннее 100 символов")
    private String title;

    @Size(max = 1000, message = "Описание должно быть не длиннее 1000 символов")
    private String description;

    @Min(value = 1, message = "Продолжительность должна быть не менее 1 минуты")
    private int durationMinutes;
}
