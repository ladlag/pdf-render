package com.finos.matcher.report.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Optional Spring Boot cache configuration using Caffeine.
 * This configuration is only active when both Spring Boot cache and Caffeine are on the classpath.
 * 
 * To enable caching in your Spring Boot application:
 * 1. Add this library with cache dependencies
 * 2. Use @Cacheable, @CachePut, @CacheEvict annotations on your service methods
 * 
 * Example:
 * <pre>
 * {@literal @}Service
 * public class MyService {
 *     {@literal @}Cacheable("pdfReports")
 *     public byte[] generateReport(ReportData data) {
 *         // expensive operation
 *     }
 * }
 * </pre>
 */
@Configuration
@EnableCaching
@ConditionalOnClass({CacheManager.class, Caffeine.class})
public class CacheConfiguration {
    
    /**
     * Configures Caffeine as the cache manager with sensible defaults:
     * - Maximum 100 entries per cache
     * - Expire after 1 hour of write
     * - Expire after 30 minutes of access
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .expireAfterAccess(30, TimeUnit.MINUTES));
        return cacheManager;
    }
}
