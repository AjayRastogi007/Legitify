package com.legitify.auth_service.service.impl;

import com.legitify.auth_service.service.AuthJwtService;
import com.legitify.common.security.AuthUser;
import com.legitify.common.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthJwtServiceImpl implements AuthJwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${JWT_ISSUER}")
    private String issuer;

    public String createAccessToken(AuthUser user) {

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(user.getUserId())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(15 * 60))
                .claim("type", "ACCESS")
                .claim("username", user.getUsername())
                .claim("email", user.getUserEmail())
                .claim("jti", UUID.randomUUID().toString())
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(claims)
        ).getTokenValue();
    }

    @Override
    public AuthUser parseAndValidate(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        return JwtUtils.extractAuthUser(jwt);
    }
}
