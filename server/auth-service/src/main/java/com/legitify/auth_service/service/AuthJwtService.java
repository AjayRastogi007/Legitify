package com.legitify.auth_service.service;

import com.legitify.common.security.AuthUser;

public interface AuthJwtService {
    String createAccessToken(AuthUser user);
    AuthUser parseAndValidate(String token);
}
