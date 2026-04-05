package com.legitify.api_gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.legitify.api_gateway.filter.JwtAuthFilter;
import com.legitify.api_gateway.service.GatewayJwtService;
import com.legitify.api_gateway.utils.JwtAuthEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    @Bean
    public JwtAuthEntryPoint jwtAuthEntryPoint(ObjectMapper objectMapper) {
        return new JwtAuthEntryPoint(objectMapper);
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(GatewayJwtService jwtService) {
        return new JwtAuthFilter(jwtService);
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            JwtAuthFilter jwtAuthFilter,
            JwtAuthEntryPoint entryPoint
    ) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint))
                .authorizeExchange(auth -> auth
                        .pathMatchers("/legitify/auth/**", "/auth/**").permitAll()
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}