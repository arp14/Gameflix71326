package com.gameflix.service;

import com.gameflix.dto.LoginRequest;
import com.gameflix.dto.LoginResponse;
import com.gameflix.model.Credential;
import com.gameflix.model.User;
import com.gameflix.repository.CredentialRepository;
import com.gameflix.repository.UserRepository;
import com.gameflix.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Targets UserService.login(), which is the minimal service surface under
 * test here: it takes a LoginRequest and, depending on user existence,
 * credential existence, and password match, either returns
 * LoginResponse.success(userId, username, token) or
 * LoginResponse.failure(message) - never throws for bad credentials.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CredentialRepository credentialRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private UserService userService;

    private UserService newUserService() {
        return new UserService(userRepository, credentialRepository, passwordEncoder, jwtService);
    }

    private static User userWithId(Long id, String username, String email, String displayName) {
        User user = new User(username, email, displayName);
        try {
            var field = User.class.getDeclaredField("userId");
            field.setAccessible(true);
            field.set(user, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    @Test
    void login_happyPath_correctCredentialsReturnSuccessWithToken() {
        userService = newUserService();
        User user = userWithId(1L, "gamer1", "gamer1@example.com", "Gamer One");
        Credential credential = new Credential(user, "$2a$10$storedHashValue");

        when(userRepository.findByUsername("gamer1")).thenReturn(Optional.of(user));
        when(credentialRepository.findByUserId(1L)).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("correct-password", "$2a$10$storedHashValue")).thenReturn(true);
        when(jwtService.generateToken(1L, "gamer1")).thenReturn("signed.jwt.token");

        LoginRequest request = new LoginRequest();
        request.setUsername("gamer1");
        request.setPassword("correct-password");

        LoginResponse response = userService.login(request);

        assertTrue(response.isSuccess());
        assertEquals(1L, response.getUserId());
        assertEquals("gamer1", response.getUsername());
        assertEquals("signed.jwt.token", response.getToken());
        assertEquals("Login successful", response.getMessage());
    }

    /**
     * Edge case: a users row exists but its matching credentials row does
     * not (e.g. a partial write elsewhere left the two tables out of sync).
     * login() must fail gracefully instead of throwing NoSuchElementException.
     */
    @Test
    void login_edgeCase_missingCredentialRecordForExistingUserReturnsFailure() {
        userService = newUserService();
        User user = userWithId(2L, "orphan", "orphan@example.com", "Orphan User");

        when(userRepository.findByUsername("orphan")).thenReturn(Optional.of(user));
        when(credentialRepository.findByUserId(2L)).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest();
        request.setUsername("orphan");
        request.setPassword("whatever");

        LoginResponse response = userService.login(request);

        assertFalse(response.isSuccess());
        assertEquals("Invalid username or password", response.getMessage());
        assertNull(response.getToken());
        verify(jwtService, never()).generateToken(anyLong(), anyString());
    }

    @Test
    void login_failureCase_wrongPasswordReturnsFailure() {
        userService = newUserService();
        User user = userWithId(1L, "gamer1", "gamer1@example.com", "Gamer One");
        Credential credential = new Credential(user, "$2a$10$storedHashValue");

        when(userRepository.findByUsername("gamer1")).thenReturn(Optional.of(user));
        when(credentialRepository.findByUserId(1L)).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("wrong-password", "$2a$10$storedHashValue")).thenReturn(false);

        LoginRequest request = new LoginRequest();
        request.setUsername("gamer1");
        request.setPassword("wrong-password");

        LoginResponse response = userService.login(request);

        assertFalse(response.isSuccess());
        assertEquals("Invalid username or password", response.getMessage());
        assertNull(response.getToken());
        verify(jwtService, never()).generateToken(anyLong(), anyString());
    }
}
