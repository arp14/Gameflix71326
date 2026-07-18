package com.gameflix.reviews.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Confirms a game exists by calling games-service's own API - this
 * service has no direct access to games_db (see db/README.md at the
 * repo root for why). A 404 from games-service means the game genuinely
 * doesn't exist; any other failure (games-service unreachable, timed
 * out, etc.) is surfaced as 503 rather than silently treated as "game
 * exists" or "game doesn't exist" - a review shouldn't be allowed to
 * reference a game we couldn't actually verify.
 */
@Component
public class GamesClient {

    private final RestClient restClient;

    public GamesClient(@Value("${games.service.url}") String gamesServiceUrl) {
        this.restClient = RestClient.create(gamesServiceUrl);
    }

    public boolean gameExists(Long gameId) {
        try {
            restClient.get()
                    .uri("/api/games/{id}", gameId)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Could not verify game with games-service", e);
        }
    }
}
