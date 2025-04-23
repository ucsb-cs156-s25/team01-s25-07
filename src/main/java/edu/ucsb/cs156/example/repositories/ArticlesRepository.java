package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Article;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The ArticlesRepository is a repository for Articles entities.
 */

@Repository
public interface ArticlesRepository extends CrudRepository<Article, Long> {
    // Custom query method to find articles by date range
    List<Article> findByDateAddedBetween(LocalDateTime start, LocalDateTime end);
}
