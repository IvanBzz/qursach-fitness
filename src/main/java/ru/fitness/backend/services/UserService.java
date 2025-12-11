package ru.fitness.backend.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fitness.backend.dto.UserRegistrationDto;
import ru.fitness.backend.exceptions.UserAlreadyExistException;
import ru.fitness.backend.models.Role;
import ru.fitness.backend.models.User;
import ru.fitness.backend.repositories.UserRepository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.List;
import jakarta.persistence.criteria.Predicate;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Retrieves the currently authenticated user from the security context.
     * @return An Optional containing the current User, or empty if not found or not authenticated.
     */
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
            return Optional.empty();
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername());
    }

    /**
     * Registers a new user based on the data from the registration DTO.
     * @param registrationDto DTO containing user registration data.
     * @throws UserAlreadyExistException if a user with the same email already exists.
     */
    @Transactional
    public void registerNewUser(UserRegistrationDto registrationDto) throws UserAlreadyExistException {
        if (userExists(registrationDto.getEmail())) {
            throw new UserAlreadyExistException("Пользователь с email " + registrationDto.getEmail() + " уже существует.");
        }

        User user = new User();
        user.setFullName(registrationDto.getFullName());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.getRoles().add(Role.ROLE_USER);
        user.setActive(true);

        userRepository.save(user);
        log.info("IN registerNewUser - user: {} successfully registered", user.getEmail());
    }

    /**
     * Checks if a user with the given email exists.
     * @param email The email to check.
     * @return true if the user exists, false otherwise.
     */
    public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Finds a user by their email.
     * @param email The email to search for.
     * @return An Optional containing the user if found.
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Finds a user by their ID.
     * @param id The ID of the user to find.
     * @return The User object.
     * @throws java.util.NoSuchElementException if the user is not found.
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Пользователь с ID " + id + " не найден."));
    }

    /**
     * Updates the roles for a specific user.
     * @param userId The ID of the user to update.
     * @param roles The new set of roles for the user.
     */
    @Transactional
    public void updateUserRoles(Long userId, java.util.Set<Role> roles) {
        User user = findById(userId);
        user.getRoles().clear();
        user.getRoles().addAll(roles);
        userRepository.save(user);
        log.info("IN updateUserRoles - user: {} roles updated", user.getEmail());
    }

    /**
     * Finds all registered users.
     * @return A list of all users.
     */
    public java.util.List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Finds users based on keyword, sort field, and sort direction.
     * @param keyword Optional keyword for searching by email or full name.
     * @param sortField Optional field to sort by (e.g., "email", "fullName").
     * @param sortDir Optional sort direction ("asc" or "desc").
     * @return A list of users.
     */
    public List<User> findUsers(String keyword, String sortField, String sortDir) {
        Sort sort = Sort.by(sortField != null && !sortField.isEmpty() ? sortField : "id");
        if (sortDir != null && sortDir.equals("desc")) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }

        Specification<User> spec = (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction(); // Always true predicate

            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), likeKeyword)
                ));
            }
            return predicate;
        };

        return userRepository.findAll(spec, sort);
    }

    /**
     * Toggles the active status of a user.
     * @param userId The ID of the user to toggle.
     * @return true if the status was changed, false if the user was not found.
     */
    @Transactional
    public boolean toggleUserStatus(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(!user.isActive());
            userRepository.save(user);
            log.info("Статус пользователя {} изменен на {}", user.getEmail(), user.isActive() ? "активен" : "неактивен");
            return true;
        }
        return false;
    }

    @Transactional
    public void updateUserProfile(Long userId, String fullName, String phoneNumber, String bio) {
        User user = findById(userId);
        user.setFullName(fullName);
        user.setPhoneNumber(phoneNumber);
        user.setBio(bio);
        userRepository.save(user);
        log.info("User profile updated for user: {}", user.getEmail());
    }

    public List<User> findTrainers() {
        return userRepository.findAllByRolesContaining(Role.ROLE_TRAINER);
    }
}
