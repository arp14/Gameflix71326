package com.gameflix.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Validates a Bearer JWT on the requests it's registered for (see
 * JwtFilterConfig) and, if valid, exposes the userId/username as request
 * attributes for downstream controllers. Registered on specific URL
 * patterns only, not globally - unrelated endpoints stay unauthenticated.
 *
 * GET requests to these paths are exempted even though the filter is
 * registered on them: browsing the game catalog and reading reviews
 * should stay open to anonymous users - only creating a game or posting
 * a review (POST) requires a token.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Set<String> OPEN_GET_PATHS = Set.of("/api/games", "/api/reviews");

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "GET".equalsIgnoreCase(request.getMethod()) && OPEN_GET_PATHS.contains(request.getRequestURI());
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
