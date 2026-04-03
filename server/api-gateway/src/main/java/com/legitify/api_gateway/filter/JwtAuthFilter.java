package com.legitify.api_gateway.filter;

import com.legitify.api_gateway.service.GatewayJwtService;
import com.legitify.common.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class JwtAuthFilter implements WebFilter {

    private final GatewayJwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);

        try {
            AuthUser authUser = jwtService.parseAndValidate(token);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            authUser,
                            null,
                            null
                    );

            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

        } catch (JwtException ex) {
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}