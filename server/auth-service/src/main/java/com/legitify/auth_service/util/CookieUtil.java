package com.legitify.auth_service.util;

import org.springframework.http.ResponseCookie;

public class CookieUtil {

    public static ResponseCookie refreshToken(String token) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .build();
    }

    public static ResponseCookie deleteRefreshToken() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .build();
    }
}
