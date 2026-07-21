package com.gameflix.controller;

import com.gameflix.dto.CreateReviewRequest;
import com.gameflix.model.Review;
import com.gameflix.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<Review> getReviews(@RequestParam Long gameId) {
        return reviewService.getReviewsForGame(gameId);
    }

    // userId comes from the JWT (set as a request attribute by
    // JwtAuthFilter), never from the request body.
    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody CreateReviewRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Review created = reviewService.createReview(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
