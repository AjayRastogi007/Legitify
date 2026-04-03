package com.legitify.api_gateway.config;

import com.legitify.common.security.EnvKeyUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwtConfiguration {

    @Bean
    public JwtDecoder jwtDecoder() throws Exception {
        RSAPublicKey publicKey =
                EnvKeyUtils.readPublicKey(System.getenv("JWT_PUBLIC_KEY_BASE64"));

        return NimbusJwtDecoder
                .withPublicKey(publicKey)
                .build();
    }
}
