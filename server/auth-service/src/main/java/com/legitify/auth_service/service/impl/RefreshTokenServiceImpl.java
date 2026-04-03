package com.legitify.auth_service.service.impl;

import com.legitify.auth_service.entity.RefreshToken;
import com.legitify.auth_service.repository.RefreshTokenRepository;
import com.legitify.auth_service.service.RefreshTokenService;
import com.legitify.auth_service.util.TokenHashUtil;
import com.legitify.common.security.AuthUser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public String createRefreshToken(AuthUser user) {
        String rawToken = UUID.randomUUID().toString();
        String hash = TokenHashUtil.sha256(rawToken);

        refreshTokenRepository.save(new RefreshToken(
                hash,
                user.getUserId(),
                Instant.now().plusSeconds(7 * 24 * 60 * 60)
        ));

        return rawToken;
    }

    public AuthUser validate(String rawToken) {
        String hash = TokenHashUtil.sha256(rawToken);

        RefreshToken refreshToken = refreshTokenRepository.findById(hash)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired");
        }

        return new AuthUser(refreshToken.getUserId(), null, null);
    }

    public void revoke(String rawToken) {
        String hash = TokenHashUtil.sha256(rawToken);
        refreshTokenRepository.deleteById(hash);
    }
}
