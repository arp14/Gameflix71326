package com.gameflix.games.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Validates a Bearer JWT on the requests it's registered for (see
 * JwtFilterConfig) and, if valid, exposes the userId/username as request
 * attributes for downstream controllers.
 *
 * Any GET under /api/games is exempted: browsing the catalog (list or a
 * single game) stays open to anonymous users, and reviews-service's
 * server-to-server existence check (GET /api/games/{id}) has no user
 * token to present in the first place. Only POST/PUT/DELETE require a
 * token.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "GET".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().startsWith("/api/games");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing bearer token");
            return;
        }

        String token = header.substring("Bearer ".length());
        try {
            Claims claims = jwtService.parseAndValidate(token);
            Number userId = claims.get("userId", Number.class);
            request.setAttribute("userId", userId.longValue());
            request.setAttribute("username", claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
