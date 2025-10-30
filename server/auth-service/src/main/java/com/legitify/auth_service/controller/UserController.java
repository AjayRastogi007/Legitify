package com.legitify.auth_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.legitify.auth_service.dto.TokenRefreshRequestDto;
import com.legitify.auth_service.dto.UserRequestDto;
import com.legitify.auth_service.dto.UserResponseDto;
import com.legitify.auth_service.service.UserService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("legitify/auth")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registration(@RequestBody UserRequestDto userRequest) {
        UserResponseDto userResponse = userService.reistration(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<UserResponseDto> signIn(@RequestBody UserRequestDto userRequest) {
        UserResponseDto userResponse = userService.signIn(userRequest);
        return ResponseEntity.status(HttpStatus.OK).body(userResponse);
    }

    @PostMapping("/sign-out")
    public ResponseEntity<String> logout(@RequestBody TokenRefreshRequestDto request) {
        userService.signOut(request);
        return ResponseEntity.ok("Sign out successfully.");
    }

}
