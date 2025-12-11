package ru.fitness.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.fitness.backend.models.Role;
import ru.fitness.backend.models.User;
import ru.fitness.backend.repositories.UserRepository;
import ru.fitness.backend.repositories.WorkoutTypeRepository; // Добавлен импорт

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import ru.fitness.backend.models.WorkoutType; // Добавлен импорт

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner demoData(UserRepository userRepository, PasswordEncoder passwordEncoder, WorkoutTypeRepository workoutTypeRepository) { // Добавлен workoutTypeRepository
		return args -> {
			// Проверка существования администратора
			Optional<User> adminOptional = userRepository.findByEmail("admin@fitness.com");
			if (adminOptional.isEmpty()) {
				System.out.println("Администратор admin@fitness.com не найден, создаем...");
				User admin = new User();
				admin.setEmail("admin@fitness.com");
				admin.setPassword(passwordEncoder.encode("password")); // Хешируем пароль
				admin.setFullName("Admin Adminov");
				admin.setActive(true);
				Set<Role> adminRoles = new HashSet<>();
				adminRoles.add(Role.ROLE_ADMIN);
				adminRoles.add(Role.ROLE_USER);
				admin.setRoles(adminRoles);
				admin.setDateOfCreated(LocalDateTime.now());
				userRepository.save(admin);
				System.out.println("Администратор admin@fitness.com создан.");
			} else {
				User admin = adminOptional.get();
				System.out.println("Администратор admin@fitness.com уже существует.");
				System.out.println("ID: " + admin.getId());
				System.out.println("Email: " + admin.getEmail());
				System.out.println("Полное имя: " + admin.getFullName());
				System.out.println("Активен: " + admin.isActive());
				System.out.println("Роли: " + admin.getRoles());
				// Дополнительная проверка пароля (для отладки)
				if (passwordEncoder.matches("password", admin.getPassword())) {
					System.out.println("Пароль 'password' для администратора admin@fitness.com совпадает с хешем.");
				} else {
					System.out.println("Пароль 'password' для администратора admin@fitness.com НЕ совпадает с хешем. Обновляем.");
					admin.setPassword(passwordEncoder.encode("password"));
					userRepository.save(admin);
					System.out.println("Пароль администратора admin@fitness.com обновлен.");
				}
			}

			// Проверка существования тренера
			Optional<User> trainerOptional = userRepository.findByEmail("trainer@fitness.com");
			if (trainerOptional.isEmpty()) {
				System.out.println("Тренер trainer@fitness.com не найден, создаем...");
				User trainer = new User();
				trainer.setEmail("trainer@fitness.com");
				trainer.setPassword(passwordEncoder.encode("password"));
				trainer.setFullName("Trainer Trenerov");
				trainer.setActive(true);
				Set<Role> trainerRoles = new HashSet<>();
				trainerRoles.add(Role.ROLE_TRAINER);
				trainerRoles.add(Role.ROLE_USER);
				trainer.setRoles(trainerRoles);
				trainer.setDateOfCreated(LocalDateTime.now());
				userRepository.save(trainer);
				System.out.println("Тренер trainer@fitness.com создан.");
			} else {
				User trainer = trainerOptional.get();
				System.out.println("Тренер trainer@fitness.com уже существует.");
				System.out.println("ID: " + trainer.getId());
				System.out.println("Email: " + trainer.getEmail());
				System.out.println("Полное имя: " + trainer.getFullName());
				System.out.println("Активен: " + trainer.isActive());
				System.out.println("Роли: " + trainer.getRoles());
				if (passwordEncoder.matches("password", trainer.getPassword())) {
					System.out.println("Пароль 'password' для тренера trainer@fitness.com совпадает с хешем.");
				} else {
					System.out.println("Пароль 'password' для тренера trainer@fitness.com НЕ совпадает с хешем. Обновляем.");
					trainer.setPassword(passwordEncoder.encode("password"));
					userRepository.save(trainer);
					System.out.println("Пароль тренера trainer@fitness.com обновлен.");
				}
			}
			// Добавление типов тренировок, если они отсутствуют
			System.out.println("Проверяем наличие типов тренировок...");
			long workoutTypeCount = workoutTypeRepository.findAll().size(); // Изменено
			if (workoutTypeCount == 0) {
				System.out.println("Типы тренировок не найдены, создаем...");
				WorkoutType yoga = new WorkoutType();
				yoga.setTitle("Йога для начинающих");
				yoga.setDescription("Отличный способ познакомиться с йогой, развить гибкость и успокоить ум.");
				yoga.setDurationMinutes(60);
				workoutTypeRepository.save(yoga);

				WorkoutType crossfit = new WorkoutType();
				crossfit.setTitle("Кроссфит");
				crossfit.setDescription("Высокоинтенсивная тренировка для тех, кто хочет проверить себя на прочность.");
				crossfit.setDurationMinutes(55);
				workoutTypeRepository.save(crossfit);

				WorkoutType pilates = new WorkoutType();
				pilates.setTitle("Пилатес");
				pilates.setDescription("Система упражнений для укрепления мышц всего тела, улучшения осанки и координации.");
				pilates.setDurationMinutes(75);
				workoutTypeRepository.save(pilates);
				System.out.println("Типы тренировок созданы.");
			} else {
				System.out.println("Типы тренировок уже существуют: " + workoutTypeCount);
			}
		};
	}
}


