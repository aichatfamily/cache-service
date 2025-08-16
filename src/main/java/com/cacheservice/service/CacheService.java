package com.cacheservice.service;

import com.cacheservice.entity.CacheEntry;
import com.cacheservice.repository.CacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CacheService {
    
    private final CacheRepository cacheRepository;
    
    public void put(String key, String value, long ttlSeconds) {
        CacheEntry entry = cacheRepository.findByKey(key)
                .orElse(new CacheEntry());
        
        entry.setKey(key);
        entry.setValue(value);
        entry.setExpiresAt(LocalDateTime.now().plusSeconds(ttlSeconds));
        
        cacheRepository.save(entry);
        log.debug("Cached entry with key: {} for {} seconds", key, ttlSeconds);
    }
    
    public void put(String key, String value) {
        CacheEntry entry = cacheRepository.findByKey(key)
                .orElse(new CacheEntry());
        
        entry.setKey(key);
        entry.setValue(value);
        entry.setExpiresAt(null); // No expiration
        
        cacheRepository.save(entry);
        log.debug("Cached entry with key: {} (no expiration)", key);
    }
    
    @Transactional(readOnly = true)
    public Optional<String> get(String key) {
        Optional<CacheEntry> entry = cacheRepository.findByKey(key);
        
        if (entry.isPresent()) {
            CacheEntry cacheEntry = entry.get();
            if (cacheEntry.isExpired()) {
                // Delete expired entry asynchronously
                deleteAsync(key);
                return Optional.empty();
            }
            return Optional.of(cacheEntry.getValue());
        }
        
        return Optional.empty();
    }
    
    public void delete(String key) {
        cacheRepository.deleteByKey(key);
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
    
    private void deleteAsync(String key) {
        // This would typically be done in a separate thread/async method
        cacheRepository.deleteByKey(key);
    }
}