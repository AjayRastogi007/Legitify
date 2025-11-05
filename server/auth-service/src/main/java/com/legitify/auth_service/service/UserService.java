package com.legitify.auth_service.service;

import com.legitify.auth_service.dto.UserRequestDto;
import com.legitify.auth_service.dto.UserResponseDto;

import jakarta.servlet.http.HttpServletRequest;

public interface UserService {
    void reistration(UserRequestDto userRequestDto);

    UserResponseDto signIn(UserRequestDto userRequestDto);

    void signOut(HttpServletRequest request);
}
