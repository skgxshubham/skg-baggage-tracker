package com.skg.bts.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.skg.bts.dto.CachedBaggageStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

// The only class that talks to Redis directly. Every failure here is caught
// and logged, never propagated — a Redis outage must never break a checkpoint
// write or a tracking read; it should just mean "no cache today."
@Service
@Slf4j
public class BaggageCacheService {

    private static final String KEY_PREFIX = "baggage:status:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public BaggageCacheService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public void put(String tagNumber, CachedBaggageStatus value, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key(tagNumber), json, ttl);
        } catch (Exception e) {
            log.warn("Redis write failed for tag {}: {}", tagNumber, e.getMessage());
        }
    }

    public CachedBaggageStatus get(String tagNumber) {
        try {
            String json = redisTemplate.opsForValue().get(key(tagNumber));
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, CachedBaggageStatus.class);
        } catch (Exception e) {
            log.warn("Redis read failed for tag {}: {}", tagNumber, e.getMessage());
            return null;
        }
    }

    private String key(String tagNumber) {
        return KEY_PREFIX + tagNumber;
    }
}