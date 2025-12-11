package ru.fitness.backend.exceptions;

public class NoAvailableSlotsException extends WorkoutSubscriptionException {
    public NoAvailableSlotsException(String message) {
        super(message);
    }
}
