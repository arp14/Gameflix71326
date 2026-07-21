package com.gameflix.repository;

import com.gameflix.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByGameIdOrderByCreatedAtDesc(Long gameId);
}
