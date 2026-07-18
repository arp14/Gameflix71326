package com.gameflix.reviews.service;

import com.gameflix.reviews.client.GamesClient;
import com.gameflix.reviews.dto.CreateReviewRequest;
import com.gameflix.reviews.model.Review;
import com.gameflix.reviews.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final GamesClient gamesClient;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, GamesClient gamesClient) {
        this.reviewRepository = reviewRepository;
        this.gamesClient = gamesClient;
    }

    public List<Review> getReviewsForGame(Long gameId) {
        return reviewRepository.findByGameIdOrderByCreatedAtDesc(gameId);
    }

    public Review createReview(Long userId, CreateReviewRequest request) {
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }
        if (!gamesClient.gameExists(request.getGameId())) {
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
