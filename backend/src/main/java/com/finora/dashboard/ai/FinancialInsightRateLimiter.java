package com.finora.dashboard.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FinancialInsightRateLimiter {

    private static final Duration WINDOW =
            Duration.ofSeconds(60);

    private static final String KEY_PREFIX =
            "finora:rate-limit:financial-insight:";

    private final StringRedisTemplate redisTemplate;

    public boolean isAllowed(UUID userId) {

        String key =
                KEY_PREFIX + userId;

        Boolean created =
                redisTemplate.opsForValue().setIfAbsent(
                        key,
                        "1",
                        WINDOW
                );

        return Boolean.TRUE.equals(created);
    }
}