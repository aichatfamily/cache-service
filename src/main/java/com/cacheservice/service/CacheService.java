package com.cacheservice.service;

import com.cacheservice.config.CacheConfig;
import com.cacheservice.entity.CacheEntry;
import com.cacheservice.repository.CacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CacheService {
    
    private final CacheRepository cacheRepository;
    private final RedisTemplate<String, String> redisTemplate;
    
    public void put(String key, String value, long ttlSeconds) {
        CacheEntry entry = cacheRepository.findByKey(key)
                .orElse(new CacheEntry());
        
        entry.setKey(key);
        entry.setValue(value);
        entry.setExpiresAt(LocalDateTime.now().plusSeconds(ttlSeconds));
        
        cacheRepository.save(entry);
        
        // Store in Redis with TTL
        try {
            String cacheKey = "ttl:" + key;
            Duration ttl = Duration.ofSeconds(ttlSeconds);
            redisTemplate.opsForValue().set(cacheKey, value, ttl);
            log.debug("Cached entry with key: {} for {} seconds", key, ttlSeconds);
        } catch (Exception e) {
            log.warn("Failed to cache TTL entry in Redis for key {}: {}", key, e.getMessage());
        }
    }
    
    @CachePut(value = CacheConfig.CACHE_NAME, key = "#key")
    public String put(String key, String value) {
        CacheEntry entry = cacheRepository.findByKey(key)
                .orElse(new CacheEntry());
        
        entry.setKey(key);
        entry.setValue(value);
        entry.setExpiresAt(null); // No expiration
        
        cacheRepository.save(entry);
        log.debug("Cached entry with key: {} (no expiration)", key);
        
        return value;
    }
    
    @Transactional(readOnly = true)
    public Optional<String> get(String key) {
        // First check TTL cache
        try {
            String ttlCacheKey = "ttl:" + key;
            String cachedValue = redisTemplate.opsForValue().get(ttlCacheKey);
            if (cachedValue != null) {
                log.debug("Cache hit in TTL cache for key: {}", key);
                return Optional.of(cachedValue);
            }
        } catch (Exception e) {
            log.warn("Failed to check TTL cache for key {}: {}", key, e.getMessage());
        }
        
        // Then check regular cache using Spring cache abstraction
        return getCachedValue(key);
    }
    
    @Cacheable(value = CacheConfig.CACHE_NAME, key = "#key")
    @Transactional(readOnly = true)
    public Optional<String> getCachedValue(String key) {
        Optional<CacheEntry> entry = cacheRepository.findByKey(key);
        
        if (entry.isPresent()) {
            CacheEntry cacheEntry = entry.get();
            if (cacheEntry.isExpired()) {
                // Delete expired entry asynchronously
                deleteAsync(key);
                return Optional.empty();
            }
            
            log.debug("Retrieved value from database for key: {}", key);
            return Optional.of(cacheEntry.getValue());
        }
        
        return Optional.empty();
    }
    
    @CacheEvict(value = CacheConfig.CACHE_NAME, key = "#key")
    public void delete(String key) {
        cacheRepository.deleteByKey(key);
        
        // Also delete from TTL cache
        try {
            String ttlCacheKey = "ttl:" + key;
            redisTemplate.delete(ttlCacheKey);
        } catch (Exception e) {
            log.warn("Failed to delete TTL cache entry for key {}: {}", key, e.getMessage());
        }
        
        log.debug("Deleted cache entry with key: {}", key);
    }
    
    public boolean exists(String key) {
        Optional<CacheEntry> entry = cacheRepository.findByKey(key);
        return entry.isPresent() && !entry.get().isExpired();
    }
    
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void cleanupExpiredEntries() {
        log.debug("Starting cleanup of expired cache entries");
        cacheRepository.deleteExpiredEntries(LocalDateTime.now());
        log.debug("Completed cleanup of expired cache entries");
    }
    
    @CacheEvict(value = CacheConfig.CACHE_NAME, key = "#key")
    private void deleteAsync(String key) {
        // This would typically be done in a separate thread/async method
        cacheRepository.deleteByKey(key);
        
        // Also delete from TTL cache
        try {
            String ttlCacheKey = "ttl:" + key;
            redisTemplate.delete(ttlCacheKey);
        } catch (Exception e) {
            log.warn("Failed to delete expired TTL cache entry for key {}: {}", key, e.getMessage());
        }
    }
}