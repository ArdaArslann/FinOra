package com.finora.dashboard.ai.service;

import com.finora.dashboard.ai.FinancialInsightGenerator;
import com.finora.dashboard.ai.FinancialInsightRateLimiter;
import com.finora.dashboard.ai.FinancialInsightRateLimitException;
import com.finora.dashboard.ai.context.FinancialInsightContext;
import com.finora.dashboard.ai.context.FinancialInsightContextBuilder;
import com.finora.dashboard.ai.prompt.FinancialInsightPromptBuilder;
import com.finora.dashboard.ai.response.FinancialInsightResponse;
import com.finora.user.entity.UserEntity;
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

    @Override
    public FinancialInsightResponse generateInsight(
            UserEntity user
    ) {

        if (!rateLimiter.isAllowed(user.getId())) {

            throw new FinancialInsightRateLimitException(
                    "Financial insight can only be requested once every 60 seconds."
            );
        }

        FinancialInsightContext context =
                contextBuilder.build(user);

        String prompt =
                promptBuilder.build(context);

        return insightGenerator.generate(prompt);
    }
}