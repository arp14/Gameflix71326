package com.gameflix.reviews.repository;

import com.gameflix.reviews.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByGameIdOrderByCreatedAtDesc(Long gameId);
}
