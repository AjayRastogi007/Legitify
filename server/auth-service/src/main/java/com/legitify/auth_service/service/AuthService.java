package com.legitify.auth_service.service;

import com.legitify.auth_service.dto.*;
import com.legitify.auth_service.entity.User;

public interface AuthService {
    AuthResult registration(RegisterRequestDto requestDto);
    AuthResult signIn(LoginRequestDto requestDto);
    AuthTokens refresh(String refreshToken);
    UserResponseDto me(String userId);
    void signOut(String refreshToken);
}
