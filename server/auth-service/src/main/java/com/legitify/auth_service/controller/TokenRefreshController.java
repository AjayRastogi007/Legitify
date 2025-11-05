package com.legitify.auth_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.legitify.auth_service.dto.TokenResponse;
import com.legitify.auth_service.entity.User;
import com.legitify.auth_service.exception.InvalidCredentialsException;
import com.legitify.auth_service.repository.UserRepository;
import com.legitify.auth_service.service.JwtService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/legitify/auth")
@AllArgsConstructor
public class TokenRefreshController {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new InvalidCredentialsException("Refresh token missing");
        }

        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        if (!jwtService.isValidRefreshToken(refreshToken, user)) {
            throw new InvalidCredentialsException("Invalid or expired refresh token");
        }

        String newAccessToken = jwtService.createAccessToken(user, "ROLE_USER");

        return ResponseEntity
                .ok()
                .body(new TokenResponse(newAccessToken));
    }
}
