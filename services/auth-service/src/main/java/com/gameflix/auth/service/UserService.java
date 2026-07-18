package com.gameflix.auth.service;

import com.gameflix.auth.dto.LoginRequest;
import com.gameflix.auth.dto.LoginResponse;
import com.gameflix.auth.dto.RegisterRequest;
import com.gameflix.auth.dto.RegisterResponse;
import com.gameflix.auth.exception.DuplicateUserException;
import com.gameflix.auth.model.Credential;
import com.gameflix.auth.model.User;
import com.gameflix.auth.repository.CredentialRepository;
import com.gameflix.auth.repository.UserRepository;
import com.gameflix.auth.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    public UserService(UserRepository userRepository,
                        CredentialRepository credentialRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUserException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("Email already registered: " + request.getEmail());
        }

        User user = new User(request.getUsername(), request.getEmail(), request.getDisplayName());
        String passwordHash = passwordEncoder.encode(request.getPassword());
        Credential credential = new Credential(user, passwordHash);
        user.setCredential(credential);

        User saved = userRepository.save(user);

        return new RegisterResponse(
                saved.getUserId(),
                saved.getUsername(),
                saved.getEmail(),
                saved.getDisplayName(),
                saved.getCreatedAt());
    }

    public LoginResponse login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            return LoginResponse.failure("Invalid username or password");
        }

        User user = userOpt.get();
        Optional<Credential> credentialOpt = credentialRepository.findByUserId(user.getUserId());
        if (credentialOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), credentialOpt.get().getPasswordHash())) {
            return LoginResponse.failure("Invalid username or password");
        }

        String token = jwtService.generateToken(user.getUserId(), user.getUsername());
        return LoginResponse.success(user.getUserId(), user.getUsername(), token);
    }
}
