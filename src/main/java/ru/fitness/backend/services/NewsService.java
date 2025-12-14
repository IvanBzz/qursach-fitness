package ru.fitness.backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fitness.backend.models.News;
import ru.fitness.backend.repositories.NewsRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;

    public List<News> getAllNews() {
        return newsRepository.findAllByOrderByPublishDateDesc();
    }

    public List<News> getLatestNews(int limit) {
        List<News> allNews = getAllNews();
        return allNews.stream().limit(limit).toList();
    }

    @Transactional
    public void createNews(String title, String content) {
        News news = new News();
        news.setTitle(title);
        news.setContent(content);
        newsRepository.save(news);
    }

    public News findById(Long id) {
        return newsRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Новость с ID " + id + " не найдена"));
    }

    @Transactional
    public void updateNews(Long id, String title, String content) {
        News news = findById(id);
        news.setTitle(title);
        news.setContent(content);
        newsRepository.save(news);
    }

    @Transactional
    public void deleteNews(Long id) {
        newsRepository.deleteById(id);
    }
}
