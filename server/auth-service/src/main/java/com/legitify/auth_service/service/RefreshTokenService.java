package com.legitify.auth_service.service;

import com.legitify.common.security.AuthUser;

public interface RefreshTokenService {
    String createRefreshToken(AuthUser user);
    AuthUser validate(String rawToken);
    void revoke(String rawToken);
}
