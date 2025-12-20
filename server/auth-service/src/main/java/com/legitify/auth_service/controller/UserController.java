package com.legitify.auth_service.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.legitify.auth_service.dto.UserRequestDto;
import com.legitify.auth_service.dto.UserResponseDto;
import com.legitify.auth_service.entity.User;
import com.legitify.auth_service.exception.InvalidCredentialsException;
import com.legitify.auth_service.mapper.UserMapper;
import com.legitify.auth_service.repository.UserRepository;
import com.legitify.auth_service.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/legitify/auth")
@AllArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<String> registration(@RequestBody UserRequestDto userRequest) {
        userService.reistration(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body("Registered successfully.");
    }

    @PostMapping("/sign-in")
    public ResponseEntity<UserResponseDto> signIn(@RequestBody UserRequestDto userRequest,
            HttpServletResponse response) {
        UserResponseDto userResponse = userService.signIn(userRequest);

        User user = userRepository.findById(userResponse.getId()).orElseThrow();
        String refreshToken = user.getRefreshToken();

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("None")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        return ResponseEntity.status(HttpStatus.OK).body(userResponse);
    }

    @PostMapping("/sign-out")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        userService.signOut(request);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok("Sign out successfully.");
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> getCurrentUser(Authentication authentication) {
        System.out.println("Me is called: " + authentication);

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        UserResponseDto response = UserMapper.MAPPER.maptoUserResponseDto(user);

        System.out.println("Me was called: " + response);

        return ResponseEntity.ok(response);
    }

}
