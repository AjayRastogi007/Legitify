package com.legitify.auth_service.service.impl;

import com.legitify.auth_service.dto.*;
import com.legitify.auth_service.service.AuthJwtService;
import com.legitify.auth_service.service.RefreshTokenService;
import com.legitify.common.security.AuthUser;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.legitify.auth_service.entity.User;
import com.legitify.auth_service.exception.EmailAlreadyExistsException;
import com.legitify.auth_service.exception.InvalidCredentialsException;
import com.legitify.auth_service.repository.UserRepository;
import com.legitify.auth_service.service.AuthService;

import lombok.AllArgsConstructor;

import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthJwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public AuthResult registration(RegisterRequestDto requestDto) {
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists: " + requestDto.getEmail());
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(requestDto.getEmail());
        user.setUsername(requestDto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(requestDto.getPassword()));
        userRepository.save(user);

        AuthTokens tokens = issueTokens(user);
        return new AuthResult(user, tokens);
    }

    @Override
    public AuthResult signIn(LoginRequestDto requestDto) {
        System.out.println("STEP A: Finding user");
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials."));

        System.out.println("STEP B: Checking password");
        boolean isPasswordMatch = passwordEncoder.matches(
                requestDto.getPassword(), user.getPasswordHash());

        if (!isPasswordMatch) {
            throw new InvalidCredentialsException("Invalid credentials.");
        }

        System.out.println("STEP C: Generating tokens");
        AuthTokens tokens = issueTokens(user);
        return new AuthResult(user, tokens);
    }

    @Transactional
    public AuthTokens refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new RuntimeException("Refresh token missing");
        }

        AuthUser authUser = refreshTokenService.validate(refreshToken);
        refreshTokenService.revoke(refreshToken);
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        return issueTokens(user);
    }

    @Override
    public UserResponseDto me(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserResponseDto(user.getId(), user.getUsername(), user.getEmail());
    }

    @Override
    public void signOut(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    private AuthTokens issueTokens(User user) {
        AuthUser authUser = new AuthUser(user.getId(), user.getUsername(), user.getEmail());

        String accessToken = jwtService.createAccessToken(authUser);
        String refreshToken = refreshTokenService.createRefreshToken(authUser);

        return new AuthTokens(accessToken, refreshToken);
    }
}
