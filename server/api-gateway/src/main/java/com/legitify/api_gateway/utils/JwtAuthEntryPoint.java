package com.legitify.api_gateway.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@RequiredArgsConstructor
public class JwtAuthEntryPoint implements ServerAuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> commence(ServerWebExchange exchange,
                               AuthenticationException ex) {

        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        var errorResponse = Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.UNAUTHORIZED.value(),
                "errorCode", "UNAUTHORIZED",
                "message", "Authentication is required to access this resource",
                "path", exchange.getRequest().getURI().getPath()
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);

            var buffer = response.bufferFactory().wrap(bytes);

            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}