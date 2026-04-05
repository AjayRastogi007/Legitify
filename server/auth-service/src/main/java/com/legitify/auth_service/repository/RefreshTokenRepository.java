package com.legitify.auth_service.repository;

import com.legitify.auth_service.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    @Transactional
    @Modifying
    void deleteByUserId(String userId);
}
