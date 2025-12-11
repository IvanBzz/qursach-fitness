package ru.fitness.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.fitness.backend.models.News;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {
    // Найти последние новости, отсортированные по дате
    List<News> findAllByOrderByPublishDateDesc();
}
