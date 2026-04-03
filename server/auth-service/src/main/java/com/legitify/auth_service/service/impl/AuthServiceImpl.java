package com.legitify.auth_service.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.legitify.auth_service.dto.UserRequestDto;
import com.legitify.auth_service.dto.UserResponseDto;
import com.legitify.auth_service.entity.User;
import com.legitify.auth_service.exception.EmailAlreadyExistsException;
import com.legitify.auth_service.exception.InvalidCredentialsException;
import com.legitify.auth_service.mapper.UserMapper;
import com.legitify.auth_service.repository.UserRepository;
import com.legitify.auth_service.service.JwtService;
import com.legitify.auth_service.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public void reistration(UserRequestDto userRequestDto) {
        if (userRepository.findByEmail(userRequestDto.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists: " + userRequestDto.getEmail());
        }

        User user = UserMapper.MAPPER.maptoUser(userRequestDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @Override
    public UserResponseDto signIn(UserRequestDto userRequestDto) {
        User user = userRepository.findByEmail(userRequestDto.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials."));

        boolean isPasswordMatch = passwordEncoder.matches(
                userRequestDto.getPassword(), user.getPassword());

        if (!isPasswordMatch) {
            throw new InvalidCredentialsException("Invalid credentials.");
        }

        String accessToken = jwtService.createAccessToken(user, "ROLE_USER");
        String refreshToken = jwtService.createRefreshToken(user, "ROLE_USER");

        user.setRefreshToken(refreshToken);
        User savedUser = userRepository.save(user);

        UserResponseDto userResponseDto = UserMapper.MAPPER.maptoUserResponseDto(savedUser);
        userResponseDto.setAccessToken(accessToken);

        return userResponseDto;
    }

    @Override
    public void signOut(HttpServletRequest request) {
        String refreshToken = extractRefreshToken(request);
        if (refreshToken == null) {
            throw new InvalidCredentialsException("Refresh token missing");
        }

        userRepository.findByRefreshToken(refreshToken)
                .ifPresent(user -> {
                    user.setRefreshToken(null);
                    userRepository.save(user);
                });
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}
