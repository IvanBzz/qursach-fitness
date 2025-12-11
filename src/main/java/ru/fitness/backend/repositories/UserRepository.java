package ru.fitness.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.fitness.backend.models.Role;
import ru.fitness.backend.models.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    /**
     * Находит пользователя по его email.
     * @param email The email to search for.
     * @return An Optional containing the user if found, or empty if not.
     */
    Optional<User> findByEmail(String email);

    /**
     * Проверяет, существует ли пользователь с данным email.
     * @param email The email to check.
     * @return true, если пользователь существует, иначе false.
     */
    boolean existsByEmail(String email);

    /**
     * Finds all users that have a specific role.
     * @param role The role to search for.
     * @return A list of users with that role.
     */
    List<User> findAllByRolesContaining(Role role);

    /**
     * Counts users registered after a given date.
     * @param date The date to compare with.
     * @return The number of users.
     */
    long countByDateOfCreatedAfter(LocalDateTime date);
}
