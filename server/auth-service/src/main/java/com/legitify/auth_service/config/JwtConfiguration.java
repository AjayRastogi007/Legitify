package com.legitify.auth_service.config;

import com.legitify.common.security.EnvKeyUtils;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwtConfiguration {

    @Bean
    public RSAKey rsaKey() throws Exception {
        RSAPrivateKey privateKey =
                EnvKeyUtils.readPrivateKey(System.getenv("JWT_PRIVATE_KEY_BASE64"));

        RSAPublicKey publicKey =
                EnvKeyUtils.readPublicKey(System.getenv("JWT_PUBLIC_KEY_BASE64"));

        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("legitify-key")
                .build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(RSAKey rsaKey) {
        var jwkSet = new JWKSet(rsaKey);
        return (selector, context) -> selector.select(jwkSet);
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }
}
