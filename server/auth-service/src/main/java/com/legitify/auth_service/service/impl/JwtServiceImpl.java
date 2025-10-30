package com.legitify.auth_service.service.impl;

import java.time.Instant;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.legitify.auth_service.entity.User;
import com.legitify.auth_service.service.JwtService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtEncoder jwtEncoder;

    @Override
    public String createAccessToken(User user, String authority) {
        var claims = JwtClaimsSet.builder()
                .issuer("auth-service")
                .issuedAt(Instant.now())
                .expiresAt((Instant.now().plusSeconds(15 * 60)))
                .subject(user.getEmail())
                .claim("scope", "ACCESS_TOKEN")
                .claim("role", authority)
                .build();

        JwtEncoderParameters parameters = JwtEncoderParameters.from(claims);
        return jwtEncoder.encode(parameters).getTokenValue();
    }

    @Override
    public String createRefreshToken(User user, String authority) {
        var claims = JwtClaimsSet.builder()
                .issuer("auth-service")
                .issuedAt(Instant.now())
                .expiresAt((Instant.now().plusSeconds(7 * 24 * 60 * 60)))
                .subject(user.getEmail())
                .claim("scope", "REFRESH_TOKEN")
                .claim("role", authority)
                .build();

        JwtEncoderParameters parameters = JwtEncoderParameters.from(claims);
        return jwtEncoder.encode(parameters).getTokenValue();
    }
}
