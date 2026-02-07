package com.cyclonex.trust_care.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    // Cache configuration for OTP storage
    // Using simple in-memory cache for OTP storage
}
