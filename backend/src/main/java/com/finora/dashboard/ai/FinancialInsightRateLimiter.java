package com.finora.dashboard.ai;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FinancialInsightRateLimiter {

    private static final Duration WINDOW =
            Duration.ofSeconds(60);

    private final ConcurrentHashMap<UUID, Instant> lastRequests =
            new ConcurrentHashMap<>();

    public boolean isAllowed(UUID userId) {

        Instant now = Instant.now();

        return lastRequests.compute(
                userId,
                (id, lastRequest) -> {

                    if (lastRequest == null) {
                        return now;
                    }

                    if (Duration.between(lastRequest, now)
                            .compareTo(WINDOW) >= 0) {

                        return now;
                    }

                    return lastRequest;
                }
        ) == now;
    }
}
