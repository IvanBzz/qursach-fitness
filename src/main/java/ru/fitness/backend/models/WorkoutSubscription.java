package ru.fitness.backend.models;

import jakarta.persistence.*;
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
@Table(name = "workout_subscription", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "schedule_id"})
})
public class WorkoutSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Column(nullable = false)
    private LocalDateTime subscriptionDate;

    public WorkoutSubscription(User user, Schedule schedule) {
        this.user = user;
        this.schedule = schedule;
        this.subscriptionDate = LocalDateTime.now();
    }
}
