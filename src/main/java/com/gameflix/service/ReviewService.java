package com.gameflix.service;

import com.gameflix.dto.CreateReviewRequest;
import com.gameflix.model.Review;
import com.gameflix.repository.GameRepository;
import com.gameflix.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final GameRepository gameRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, GameRepository gameRepository) {
        this.reviewRepository = reviewRepository;
        this.gameRepository = gameRepository;
    }

    public List<Review> getReviewsForGame(Long gameId) {
        return reviewRepository.findByGameIdOrderByCreatedAtDesc(gameId);
    }

    public Review createReview(Long userId, CreateReviewRequest request) {
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }
        // Direct repository check, not a live HTTP call: the monolith
        // has the games table in the same database, unlike
        // reviews-service in the microservices version (see its
        // GamesClient for the equivalent check across schemas).
        if (!gameRepository.existsById(request.getGameId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }

        Review review = new Review(request.getGameId(), userId, request.getRating(), request.getComment());
        try {
            return reviewRepository.save(review);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You've already reviewed this game");
        }
    }
}
