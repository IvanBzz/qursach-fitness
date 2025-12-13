-- This file is executed on startup by Spring Boot.
-- Passwords for all users are 'password'.
-- BCrypt hash for 'password' is '$2a$10$8.UnVuG9HHgffUDAlk8qYOjzdV.PaEVOPgeGKCEnmuh.ifMbX.7b6'

-- Create Users (Admin, Trainer, User)
INSERT INTO users (id, email, password, full_name, phone_number, bio, active, date_of_created)
VALUES (1, 'admin@fitness.com', '$2a$10$8.UnVuG9HHgffUDAlk8qYOjzdV.PaEVOPgeGKCEnmuh.ifMbX.7b6', 'Admin Adminov', '+79990000000', 'Главный администратор системы.', true, NOW()),
       (2, 'trainer@fitness.com', '$2a$10$8.UnVuG9HHgffUDAlk8qYOjzdV.PaEVOPgeGKCEnmuh.ifMbX.7b6', 'Trainer Trenerov', '+79991112233', 'Сертифицированный тренер с 5-летним стажем. Специализируюсь на функциональных тренировках и кроссфите.', true, NOW()),
       (3, 'user@fitness.com', '$2a$10$8.UnVuG9HHgffUDAlk8qYOjzdV.PaEVOPgeGKCEnmuh.ifMbX.7b6', 'User Userov', '+79995556677', 'Клиент клуба.', true, NOW())
ON CONFLICT (email) DO UPDATE SET full_name = EXCLUDED.full_name, phone_number = EXCLUDED.phone_number, bio = EXCLUDED.bio;

-- Assign Roles
INSERT INTO user_role (user_id, roles)
VALUES (1, 'ROLE_ADMIN'),
       (1, 'ROLE_USER'),
       (2, 'ROLE_TRAINER'),
       (2, 'ROLE_USER'),
       (3, 'ROLE_USER')
ON CONFLICT (user_id, roles) DO NOTHING;

-- Create Workout Types
INSERT INTO workout_type (id, title, description, duration_minutes)
VALUES (1, 'Йога для начинающих', 'Отличный способ познакомиться с йогой, развить гибкость и успокоить ум.', 60),
       (2, 'Кроссфит', 'Высокоинтенсивная тренировка для тех, кто хочет проверить себя на прочность.', 55),
       (3, 'Пилатес', 'Система упражнений для укрепления мышц всего тела, улучшения осанки и координации.', 75)
ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, description = EXCLUDED.description, duration_minutes = EXCLUDED.duration_minutes;

-- Create Schedule Entries for the next few days
-- Note: Timestamps might need adjustment based on the current date when you run this.
INSERT INTO schedule (workout_id, trainer_id, start_time, available_slots, total_slots)
VALUES (1, 2, NOW() + INTERVAL '1 day', 10, 10), -- Yoga tomorrow
       (2, 2, NOW() + INTERVAL '2 day', 15, 15)  -- CrossFit the day after
ON CONFLICT DO NOTHING;

-- To avoid sequence issues after manual insertion
-- SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
-- SELECT setval('workout_type_id_seq', (SELECT MAX(id) FROM workout_type));
-- SELECT setval('schedule_id_seq', (SELECT MAX(id) FROM schedule));
