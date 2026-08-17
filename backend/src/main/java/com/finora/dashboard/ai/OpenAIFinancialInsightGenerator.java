package com.finora.dashboard.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finora.dashboard.ai.response.FinancialInsightResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class OpenAIFinancialInsightGenerator
        implements FinancialInsightGenerator {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public OpenAIFinancialInsightGenerator(
            ObjectMapper objectMapper,
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model}") String model
    ) {
        this.objectMapper = objectMapper;
        this.model = model;

        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(
                        "Authorization",
                        "Bearer " + apiKey
                )
                .build();
    }

    @Override
    public FinancialInsightResponse generate(String prompt) {

        long start = System.currentTimeMillis();

        

        Map<String, Object> content =
                Map.of(
                        "role", "user",
                        "content", List.of(
                                Map.of(
                                        "type", "input_text",
                                        "text", prompt
                                )
                        )
                );

        Map<String, Object> textFormat =
                Map.of(
                        "type", "json_schema",
                        "name", "financial_insight",
                        "strict", true,
                        "schema", Map.of(
                                "type", "object",
                                "additionalProperties", false,
                                "properties", Map.of(
                                        "summary",
                                        Map.of(
                                                "type", "string"
                                        ),

                                        "monthlyStatus",
                                        Map.of(
                                                "type", "object",
                                                "additionalProperties", false,
                                                "properties", Map.of(
                                                        "income",
                                                        Map.of(
                                                                "type", "number"
                                                        ),
                                                        "expenses",
                                                        Map.of(
                                                                "type", "number"
                                                        ),
                                                        "balance",
                                                        Map.of(
                                                                "type", "number"
                                                        )
                                                ),
                                                "required", List.of(
                                                        "income",
                                                        "expenses",
                                                        "balance"
                                                )
                                        ),

                                        "budgetInsights",
                                        Map.of(
                                                "type", "array",
                                                "items", Map.of(
                                                        "type", "object",
                                                        "additionalProperties", false,
                                                        "properties", Map.of(
                                                                "category",
                                                                Map.of(
                                                                        "type", "string"
                                                                ),
                                                                "spent",
                                                                Map.of(
                                                                        "type", "number"
                                                                ),
                                                                "budget",
                                                                Map.of(
                                                                        "type", "number"
                                                                ),
                                                                "remaining",
                                                                Map.of(
                                                                        "type", "number"
                                                                ),
                                                                "usagePercentage",
                                                                Map.of(
                                                                        "type", "integer"
                                                                )
                                                        ),
                                                        "required", List.of(
                                                                "category",
                                                                "spent",
                                                                "budget",
                                                                "remaining",
                                                                "usagePercentage"
                                                        )
                                                )
                                        ),

                                        "recommendations",
                                        Map.of(
                                                "type", "array",
                                                "items", Map.of(
                                                        "type", "string"
                                                )
                                        )
                                ),
                                "required", List.of(
                                        "summary",
                                        "monthlyStatus",
                                        "budgetInsights",
                                        "recommendations"
                                )
                        )
                );

        Map<String, Object> request =
                Map.of(
                        "model", model,
                        "input", List.of(content),
                        "text", Map.of(
                                "format", textFormat
                        )
                );

        long requestStart = System.currentTimeMillis();

        String response =
                restClient.post()
                        .uri("/responses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(String.class);

        long requestEnd = System.currentTimeMillis();

        

        FinancialInsightResponse result =
                parseResponse(response);

        long end = System.currentTimeMillis();

        

        return result;
    }

    private FinancialInsightResponse parseResponse(
            String response
    ) {

        try {

            JsonNode root =
                    objectMapper.readTree(response);

            JsonNode output =
                    root.path("output");

            for (JsonNode item : output) {

                JsonNode content =
                        item.path("content");

                for (JsonNode contentItem : content) {

                    if ("output_text".equals(
                            contentItem.path("type").asText()
                    )) {

                        String text =
                                contentItem
                                        .path("text")
                                        .asText();

                        return objectMapper.readValue(
                                text,
                                FinancialInsightResponse.class
                        );
                    }
                }
            }

            throw new IllegalStateException(
                    "No output text found in OpenAI response."
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to parse financial insight response.",
                    e
            );
        }
    }
}