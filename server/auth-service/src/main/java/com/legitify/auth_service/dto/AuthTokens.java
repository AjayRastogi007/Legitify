package com.legitify.auth_service.dto;

public record AuthTokens(
        String accessToken,
        String refreshToken
) {
}
