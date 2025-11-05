package com.legitify.auth_service.service;

import com.legitify.auth_service.entity.User;

public interface JwtService {
    String createAccessToken(User user, String authority);
    String createRefreshToken(User user, String authority);
    String extractEmail(String token);
    boolean isValidRefreshToken(String token, User user);
}
