package com.finos.matcher.report.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
 * Cache settings can be customized via application.yml:
 * <pre>
 * pdf-render:
 *   cache:
 *     maximum-size: 100
 *     expire-after-write-minutes: 60
 *     expire-after-access-minutes: 30
 * </pre>
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
@EnableConfigurationProperties(PdfRenderProperties.class)
public class CacheConfiguration {
    
    private final PdfRenderProperties properties;
    
    public CacheConfiguration(PdfRenderProperties properties) {
        this.properties = properties;
    }
    
    /**
     * Configures Caffeine as the cache manager with settings from properties.
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(properties.getCache().getMaximumSize())
                .expireAfterWrite(properties.getCache().getExpireAfterWriteMinutes(), TimeUnit.MINUTES)
                .expireAfterAccess(properties.getCache().getExpireAfterAccessMinutes(), TimeUnit.MINUTES));
        return cacheManager;
    }
}
