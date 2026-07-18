package com.gameflix.reviews.model;

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
 * game_id and user_id are plain references, not foreign keys - they
 * point at rows in games_db/auth_db, databases this service has no
 * direct access to. game_id is validated by calling games-service's API
 * (see GamesClient); user_id comes straight from an already-verified JWT
 * claim, not a lookup. See db/README.md at the repo root for the full
 * reasoning.
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
