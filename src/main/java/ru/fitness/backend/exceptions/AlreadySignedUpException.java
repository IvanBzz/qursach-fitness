package ru.fitness.backend.exceptions;

public class AlreadySignedUpException extends WorkoutSubscriptionException {
    public AlreadySignedUpException(String message) {
        super(message);
    }
}
