package com.legitify.api_gateway.config;

import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {
    
    @Bean
    public CaffeineCacheManager cacheManager() {
        return new CaffeineCacheManager("serviceInstances");
    }
}
