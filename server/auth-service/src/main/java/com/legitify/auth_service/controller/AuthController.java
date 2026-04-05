package com.legitify.auth_service.controller;

import com.legitify.auth_service.dto.*;
import com.legitify.auth_service.service.AuthJwtService;
import com.legitify.auth_service.util.CookieUtil;
import com.legitify.common.security.AuthUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.legitify.auth_service.mapper.UserMapper;
import com.legitify.auth_service.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/legitify/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthJwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> registration(@RequestBody RegisterRequestDto requestDto, HttpServletResponse response) {

        AuthResult result = authService.registration(requestDto);

        response.addHeader("Set-Cookie",
                CookieUtil.refreshToken(result.tokens().refreshToken()).toString()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                UserMapper.MAPPER.toAuthResponseDto(
                        result.user(),
                        result.tokens().accessToken()
                )
        );
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponseDto> signIn(@RequestBody LoginRequestDto requestDto,
            HttpServletResponse response) {
        AuthResult result = authService.signIn(requestDto);

        response.addHeader("Set-Cookie",
                CookieUtil.refreshToken(result.tokens().refreshToken()).toString()
        );

        return ResponseEntity.status(HttpStatus.OK).body(
                UserMapper.MAPPER.toAuthResponseDto(
                        result.user(),
                        result.tokens().accessToken()
                )
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        AuthTokens tokens = authService.refresh(refreshToken);

        response.addHeader("Set-Cookie",
                CookieUtil.refreshToken(tokens.refreshToken()).toString()
        );

        return ResponseEntity.status(HttpStatus.OK).body(new TokenResponseDto(tokens.accessToken()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> me(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        AuthUser user = jwtService.parseAndValidate(token);
        return ResponseEntity.ok(authService.me(user.getUserId()));
    }

    @PostMapping("/sign-out")
    public ResponseEntity<Void> signOut(@CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        authService.signOut(refreshToken);

        response.addHeader("Set-Cookie",
                CookieUtil.deleteRefreshToken().toString()
        );

        return ResponseEntity.noContent().build();
    }

}
