package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.MenuItemReview;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuItemReviewRepository extends CrudRepository<MenuItemReview, Long> {
    Iterable<MenuItemReview> findByItemId(long itemId);

    Iterable<MenuItemReview> findByReviewerEmail(String reviewerEmail);

    Iterable<MenuItemReview> findByItemIdAndReviewerEmail(long itemId, String reviewerEmail);
}