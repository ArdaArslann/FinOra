package com.finora.dashboard.ai;

public class FinancialInsightRateLimitException
        extends RuntimeException {

    public FinancialInsightRateLimitException(
            String message
    ) {
        super(message);
    }
}