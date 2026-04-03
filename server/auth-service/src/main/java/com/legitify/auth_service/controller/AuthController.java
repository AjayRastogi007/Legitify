package com.legitify.auth_service.controller;

import com.legitify.auth_service.dto.*;
import com.legitify.auth_service.util.CookieUtil;
import com.legitify.common.security.AuthUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> me(Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        assert user != null;
        UserResponseDto response = authService.me(user.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
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
