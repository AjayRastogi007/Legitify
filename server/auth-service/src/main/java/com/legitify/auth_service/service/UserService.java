package com.legitify.auth_service.service;

import com.legitify.auth_service.dto.TokenRefreshRequestDto;
import com.legitify.auth_service.dto.UserRequestDto;
import com.legitify.auth_service.dto.UserResponseDto;

public interface UserService {
    UserResponseDto reistration(UserRequestDto userRequestDto);
    UserResponseDto signIn(UserRequestDto userRequestDto);
    void signOut(TokenRefreshRequestDto tokenRefreshRequestDto);
}
