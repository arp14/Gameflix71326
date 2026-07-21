package com.gameflix.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Check;

import java.time.Instant;

/**
 * Same shape as reviews-service's Review (services/reviews-service) so
 * the same frontend code works against either backend. game_id/user_id
 * are kept as plain Longs here too, not JPA relations - not because the
 * monolith couldn't enforce real foreign keys (it could; everything is
 * one database), but so the JSON response shape matches reviews-service
 * exactly. The actual difference between the two versions is how
 * "does this game exist" gets checked: a direct repository query here,
 * versus a live HTTP call to games-service in the microservices version
 * (see ReviewService).
 */
@Entity
@Table(name = "reviews", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"game_id", "user_id"})
})
@Check(constraints = "rating BETWEEN 1 AND 5")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Review() {
    }

    public Review(Long gameId, Long userId, Integer rating, String comment) {
        this.gameId = gameId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getGameId() {
        return gameId;
    }

    public Long getUserId() {
        return userId;
    }

    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
