package com.legitify.api_gateway.service;

import com.legitify.common.security.AuthUser;
import com.legitify.common.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GatewayJwtService {
    private final JwtDecoder jwtDecoder;

    public AuthUser parseAndValidate(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        return JwtUtils.extractAuthUser(jwt);
    }
}
