package com.finora.dashboard.ai.service;

import com.finora.dashboard.ai.FinancialInsightGenerator;
import com.finora.dashboard.ai.FinancialInsightRateLimiter;
import com.finora.dashboard.ai.FinancialInsightRateLimitException;
import com.finora.dashboard.ai.context.FinancialInsightContext;
import com.finora.dashboard.ai.context.FinancialInsightContextBuilder;
import com.finora.dashboard.ai.prompt.FinancialInsightPromptBuilder;
import com.finora.dashboard.ai.response.FinancialInsightResponse;
import com.finora.user.entity.UserEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FinancialInsightServiceImpl
        implements FinancialInsightService {

    private final FinancialInsightContextBuilder contextBuilder;
    private final FinancialInsightPromptBuilder promptBuilder;
    private final FinancialInsightGenerator insightGenerator;
    private final FinancialInsightRateLimiter rateLimiter;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String CACHE_KEY_PREFIX = "finora:ai-insight:cache:";

    @Override
    public FinancialInsightResponse generateInsight(
            UserEntity user
    ) {
        String cacheKey = CACHE_KEY_PREFIX + user.getId();
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return objectMapper.readValue(cached, FinancialInsightResponse.class);
            }
        } catch (Exception e) {
            // ignore cache errors
        }

        if (!rateLimiter.isAllowed(user.getId())) {
            throw new FinancialInsightRateLimitException(
                    "Financial insight can only be requested once every 60 seconds."
            );
        }

        FinancialInsightContext context =
                contextBuilder.build(user);

        String prompt =
                promptBuilder.build(context);

        FinancialInsightResponse insight = insightGenerator.generate(prompt);
        try {
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(insight));
        } catch (Exception e) {
            // ignore
        }
        return insight;
    }
}