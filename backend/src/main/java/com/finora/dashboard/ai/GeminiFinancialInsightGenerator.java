package com.finora.dashboard.ai;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import com.finora.dashboard.ai.response.FinancialInsightResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Primary
public class GeminiFinancialInsightGenerator
        implements FinancialInsightGenerator {

    private final Client client;
    private final ObjectMapper objectMapper;
    private final String model;

    public GeminiFinancialInsightGenerator(
            ObjectMapper objectMapper,
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model
    ) {
        this.objectMapper = objectMapper;

        this.client = Client.builder()
                .apiKey(apiKey)
                .build();

        this.model = model;
    }

    @Override
    public FinancialInsightResponse generate(String prompt) {

        long start = System.currentTimeMillis();

        System.out.println("GEMINI REQUEST START");

        try {

            return executeRequest(prompt);

        } catch (Exception firstException) {

            if (!isRetryable(firstException)) {

                throw new GeminiInsightException(
                        "Gemini request failed.",
                        firstException
                );
            }

            System.err.println(
                    "GEMINI REQUEST FAILED. " +
                            "Retrying in 5 seconds..."
            );

            System.err.println(
                    "Reason: " +
                            firstException.getMessage()
            );

            waitBeforeRetry();

            try {

                System.out.println("GEMINI RETRY START");

                return executeRequest(prompt);

            } catch (Exception secondException) {

                throw new GeminiInsightException(
                        "Gemini request failed after retry.",
                        secondException
                );
            }

        } finally {

            long elapsed =
                    System.currentTimeMillis() - start;

            System.out.println(
                    "TOTAL GEMINI TIME: "
                            + elapsed
                            + " ms"
            );
        }
    }

    private FinancialInsightResponse executeRequest(
            String prompt
    ) {

        long requestStart =
                System.currentTimeMillis();

        GenerateContentConfig config =
                GenerateContentConfig.builder()
                        .responseMimeType("application/json")
                        .responseSchema(buildResponseSchema())
                        .build();

        GenerateContentResponse response =
                client.models.generateContent(
                        model,
                        prompt,
                        config
                );

        long elapsed =
                System.currentTimeMillis()
                        - requestStart;

        System.out.println(
                "GEMINI REQUEST TIME: "
                        + elapsed
                        + " ms"
        );

        try {

            return objectMapper.readValue(
                    response.text(),
                    FinancialInsightResponse.class
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to parse Gemini financial insight response.",
                    e
            );
        }
    }

    private boolean isRetryable(Exception exception) {

        String message =
                exception.getMessage();

        if (message == null) {
            return true;
        }

        String lower =
                message.toLowerCase();

        return lower.contains("timeout")
                || lower.contains("timed out")
                || lower.contains("unknownhost")
                || lower.contains("connection")
                || lower.contains("503")
                || lower.contains("502")
                || lower.contains("500")
                || lower.contains("429")
                || lower.contains("rate limit");
    }

    private void waitBeforeRetry() {

        try {

            Thread.sleep(5000);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new GeminiInsightException(
                    "Gemini retry was interrupted.",
                    e
            );
        }
    }


    private Schema buildResponseSchema() {

        Schema monthlyStatusSchema =
                Schema.builder()
                        .type(Type.Known.OBJECT)
                        .properties(
                                Map.of(
                                        "income",
                                        Schema.builder()
                                                .type(Type.Known.NUMBER)
                                                .build(),

                                        "expenses",
                                        Schema.builder()
                                                .type(Type.Known.NUMBER)
                                                .build(),

                                        "balance",
                                        Schema.builder()
                                                .type(Type.Known.NUMBER)
                                                .build()
                                )
                        )
                        .required(
                                List.of(
                                        "income",
                                        "expenses",
                                        "balance"
                                )
                        )
                        .build();

        Schema budgetInsightSchema =
                Schema.builder()
                        .type(Type.Known.OBJECT)
                        .properties(
                                Map.of(
                                        "category",
                                        Schema.builder()
                                                .type(Type.Known.STRING)
                                                .build(),

                                        "spent",
                                        Schema.builder()
                                                .type(Type.Known.NUMBER)
                                                .build(),

                                        "budget",
                                        Schema.builder()
                                                .type(Type.Known.NUMBER)
                                                .build(),

                                        "remaining",
                                        Schema.builder()
                                                .type(Type.Known.NUMBER)
                                                .build(),

                                        "usagePercentage",
                                        Schema.builder()
                                                .type(Type.Known.INTEGER)
                                                .build()
                                )
                        )
                        .required(
                                List.of(
                                        "category",
                                        "spent",
                                        "budget",
                                        "remaining",
                                        "usagePercentage"
                                )
                        )
                        .build();

        Schema budgetInsightsArray =
                Schema.builder()
                        .type(Type.Known.ARRAY)
                        .items(budgetInsightSchema)
                        .build();

        Schema recommendationsArray =
                Schema.builder()
                        .type(Type.Known.ARRAY)
                        .items(
                                Schema.builder()
                                        .type(Type.Known.STRING)
                                        .build()
                        )
                        .build();

        return Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(
                        Map.of(
                                "summary",
                                Schema.builder()
                                        .type(Type.Known.STRING)
                                        .build(),

                                "monthlyStatus",
                                monthlyStatusSchema,

                                "budgetInsights",
                                budgetInsightsArray,

                                "recommendations",
                                recommendationsArray
                        )
                )
                .required(
                        List.of(
                                "summary",
                                "monthlyStatus",
                                "budgetInsights",
                                "recommendations"
                        )
                )
                .build();
    }
}