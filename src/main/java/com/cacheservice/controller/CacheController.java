package com.cacheservice.controller;

import com.cacheservice.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
public class CacheController {
    
    private final CacheService cacheService;
    
    @GetMapping("/{key}")
    public ResponseEntity<Map<String, String>> get(@PathVariable String key) {
        Optional<String> value = cacheService.get(key);
        
        if (value.isPresent()) {
            return ResponseEntity.ok(Map.of("key", key, "value", value.get()));
        }
        
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/{key}")
    public ResponseEntity<Map<String, String>> put(
            @PathVariable String key,
            @RequestBody Map<String, Object> request) {
        
        String value = (String) request.get("value");
        Object ttlObj = request.get("ttl");
        
        if (ttlObj != null) {
            long ttl = ((Number) ttlObj).longValue();
            cacheService.put(key, value, ttl);
        } else {
            cacheService.put(key, value);
        }
        
        return ResponseEntity.ok(Map.of("message", "Cache entry created", "key", key));
    }
    
    @DeleteMapping("/{key}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String key) {
        cacheService.delete(key);
        return ResponseEntity.ok(Map.of("message", "Cache entry deleted", "key", key));
    }
    
    @GetMapping("/{key}/exists")
    public ResponseEntity<Map<String, Boolean>> exists(@PathVariable String key) {
        boolean exists = cacheService.exists(key);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}