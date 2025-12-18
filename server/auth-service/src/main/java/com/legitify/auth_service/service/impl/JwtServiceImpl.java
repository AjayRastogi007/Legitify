package com.legitify.auth_service.service.impl;

import java.text.ParseException;
import java.time.Instant;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.legitify.auth_service.entity.User;
import com.legitify.auth_service.service.JwtService;
import com.nimbusds.jwt.SignedJWT;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtEncoder jwtEncoder;

    @Override
    public String createAccessToken(User user, String authority) {
        var claims = JwtClaimsSet.builder()
                .issuer("http://localhost:8081")
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
                .issuer("http://localhost:8081")
                .issuedAt(Instant.now())
                .expiresAt((Instant.now().plusSeconds(7 * 24 * 60 * 60)))
                .subject(user.getEmail())
                .claim("scope", "REFRESH_TOKEN")
                .claim("role", authority)
                .build();

        JwtEncoderParameters parameters = JwtEncoderParameters.from(claims);
        return jwtEncoder.encode(parameters).getTokenValue();
    }

    @Override
    public String extractEmail(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    @Override
    public boolean isValidRefreshToken(String token, User user) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            Instant expiration = signedJWT
                    .getJWTClaimsSet()
                    .getExpirationTime()
                    .toInstant();
            Instant now = Instant.now();

            if (expiration.isBefore(now)) {
                return false;
            }

            String email = signedJWT.getJWTClaimsSet().getSubject();
            if (!email.equals(user.getEmail())) {
                return false;
            }

            String scope = signedJWT.getJWTClaimsSet().getStringClaim("scope");
            return "REFRESH_TOKEN".equals(scope);

        } catch (ParseException | NullPointerException e) {
            return false;
        }
    }
}
