package com.cacheservice.repository;

import com.cacheservice.entity.CacheEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CacheRepository extends JpaRepository<CacheEntry, Long> {
    
    Optional<CacheEntry> findByKey(String key);
    
    void deleteByKey(String key);
    
    @Modifying
    @Query("DELETE FROM CacheEntry c WHERE c.expiresAt < :now")
    void deleteExpiredEntries(LocalDateTime now);
    
    boolean existsByKey(String key);
}