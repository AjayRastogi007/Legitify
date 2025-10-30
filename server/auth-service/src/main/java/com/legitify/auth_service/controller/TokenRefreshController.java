package com.legitify.auth_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.legitify.auth_service.dto.AuthResponseDto;
import com.legitify.auth_service.dto.TokenRefreshRequestDto;
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
    public ResponseEntity<AuthResponseDto> refresh(@RequestBody TokenRefreshRequestDto request) {
        String refreshToken = request.getRefreshToken();

        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid or expired JWT token"));

        String newAccessToken = jwtService.createAccessToken(user, "ROLE_USER");
        String newRefreshToken = jwtService.createRefreshToken(user, "ROLE_USER");
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return ResponseEntity.ok(new AuthResponseDto(newAccessToken, newRefreshToken));
    }
}
