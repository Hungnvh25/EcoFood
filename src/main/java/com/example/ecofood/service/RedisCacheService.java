package com.example.ecofood.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisCacheService {

    StringRedisTemplate redisTemplate;

    public void set(String key, String value, Long time, TimeUnit unit) {
        if (time == null || time <= 0) {
            redisTemplate.opsForValue().set(key, value);
        } else {
            redisTemplate.opsForValue().set(key, value, time, unit);
        }
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void update(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }
    public void delete(String key) {
        redisTemplate.delete(key);
    }


}
