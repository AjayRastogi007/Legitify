package com.legitify.auth_service.dto;

import com.legitify.auth_service.entity.User;

public record AuthResult(
        User user,
        AuthTokens tokens) {
}
