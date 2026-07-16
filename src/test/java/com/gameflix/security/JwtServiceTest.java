package com.gameflix.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String TEST_SECRET = "this-is-a-test-secret-value-32chars-min!";

    @Test
    void generatesTokenWithExpectedClaimsAndValidSignature() {
        JwtService jwtService = new JwtService(TEST_SECRET, 60_000);

        String token = jwtService.generateToken(42L, "gamer1");
        assertEquals(3, token.split("\\.").length, "a JWT is header.payload.signature");

        Claims claims = jwtService.parseAndValidate(token);
        assertEquals("gamer1", claims.getSubject());
        assertEquals(42, claims.get("userId", Number.class).intValue());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    @Test
    void rejectsExpiredToken() throws InterruptedException {
        JwtService jwtService = new JwtService(TEST_SECRET, 1);
        String token = jwtService.generateToken(1L, "expiredUser");

        Thread.sleep(50);

        assertThrows(ExpiredJwtException.class, () -> jwtService.parseAndValidate(token));
    }

    @Test
    void rejectsTokenSignedWithADifferentSecret() {
        JwtService signer = new JwtService(TEST_SECRET, 60_000);
        JwtService verifier = new JwtService("a-completely-different-secret-value-32ch", 60_000);

        String token = signer.generateToken(1L, "someone");

        assertThrows(JwtException.class, () -> verifier.parseAndValidate(token));
    }

    @Test
    void rejectsGarbageToken() {
        JwtService jwtService = new JwtService(TEST_SECRET, 60_000);
        assertThrows(JwtException.class, () -> jwtService.parseAndValidate("not.a.jwt"));
    }
}
