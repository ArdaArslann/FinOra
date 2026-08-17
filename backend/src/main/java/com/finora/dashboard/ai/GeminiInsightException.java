package com.finora.dashboard.ai;

public class GeminiInsightException
        extends RuntimeException {

    public GeminiInsightException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}