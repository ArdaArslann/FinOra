package com.finora.receipt.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finora.receipt.service.ReceiptExtractionResult;
import com.finora.receipt.service.ReceiptExtractor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIReceiptExtractor implements ReceiptExtractor {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public OpenAIReceiptExtractor(
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
    public ReceiptExtractionResult extract(
            byte[] file,
            String contentType
    ) {

        String base64File =
                Base64.getEncoder().encodeToString(file);

        String dataUrl =
                "data:" + contentType + ";base64," + base64File;

        Map<String, Object> request =
                buildRequest(dataUrl);

        String response =
                restClient.post()
                        .uri("/responses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(String.class);

        return parseResponse(response);
    }

    private Map<String, Object> buildRequest(
            String dataUrl
    ) {

        Map<String, Object> image =
                Map.of(
                        "type", "input_image",
                        "image_url", dataUrl
                );

        Map<String, Object> text =
                Map.of(
                        "type", "input_text",
                        "text", """
                                Analyze this receipt.

                                Extract:
                                - merchant name
                                - total amount
                                - transaction date
                                - currency
                                - suggested expense category

                                Rules:
                                - Return null when a value cannot be determined.
                                - transactionDate must use YYYY-MM-DD.
                                - totalAmount must be a numeric value.
                                - suggestedCategory must be one of:
                                  Food, Transport, Shopping, Bills,
                                  Entertainment, Health, Other.
                                """
                );

        Map<String, Object> content =
                Map.of(
                        "role", "user",
                        "content", List.of(
                                text,
                                image
                        )
                );

        Map<String, Object> schema =
                new HashMap<>();

        schema.put("type", "object");

        schema.put(
                "properties",
                Map.of(
                        "merchantName",
                        Map.of(
                                "type",
                                List.of("string", "null")
                        ),
                        "totalAmount",
                        Map.of(
                                "type",
                                List.of("number", "null")
                        ),
                        "transactionDate",
                        Map.of(
                                "type",
                                List.of("string", "null")
                        ),
                        "currency",
                        Map.of(
                                "type",
                                List.of("string", "null")
                        ),
                        "suggestedCategory",
                        Map.of(
                                "type",
                                List.of("string", "null")
                        )
                )
        );

        schema.put(
                "required",
                List.of(
                        "merchantName",
                        "totalAmount",
                        "transactionDate",
                        "currency",
                        "suggestedCategory"
                )
        );

        schema.put("additionalProperties", false);

        Map<String, Object> format =
                Map.of(
                        "type", "json_schema",
                        "name", "receipt_extraction",
                        "strict", true,
                        "schema", schema
                );

        Map<String, Object> textConfig =
                Map.of(
                        "format", format
                );

        return Map.of(
                "model", model,
                "input", List.of(content),
                "text", textConfig
        );
    }

    private ReceiptExtractionResult parseResponse(
            String response
    ) {

        try {

            JsonNode root =
                    objectMapper.readTree(response);

            String jsonText =
                    findOutputText(root);

            JsonNode extracted =
                    objectMapper.readTree(jsonText);

            String merchantName =
                    nullableText(
                            extracted,
                            "merchantName"
                    );

            BigDecimal totalAmount =
                    nullableDecimal(
                            extracted,
                            "totalAmount"
                    );

            LocalDate transactionDate =
                    nullableDate(
                            extracted,
                            "transactionDate"
                    );

            String currency =
                    nullableText(
                            extracted,
                            "currency"
                    );

            String suggestedCategory =
                    nullableText(
                            extracted,
                            "suggestedCategory"
                    );

            return new ReceiptExtractionResult(
                    merchantName,
                    totalAmount,
                    transactionDate,
                    currency,
                    suggestedCategory
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to parse receipt extraction response.",
                    e
            );
        }
    }

    private String findOutputText(
            JsonNode root
    ) {

        JsonNode output =
                root.path("output");

        for (JsonNode item : output) {

            JsonNode content =
                    item.path("content");

            for (JsonNode contentItem : content) {

                if ("output_text".equals(
                        contentItem.path("type").asText()
                )) {
                    return contentItem
                            .path("text")
                            .asText();
                }
            }
        }

        throw new IllegalStateException(
                "No output text found in OpenAI response."
        );
    }

    private String nullableText(
            JsonNode node,
            String field
    ) {

        JsonNode value = node.get(field);

        if (value == null || value.isNull()) {
            return null;
        }

        return value.asText();
    }

    private BigDecimal nullableDecimal(
            JsonNode node,
            String field
    ) {

        JsonNode value = node.get(field);

        if (value == null || value.isNull()) {
            return null;
        }

        return value.decimalValue();
    }

    private LocalDate nullableDate(
            JsonNode node,
            String field
    ) {

        JsonNode value = node.get(field);

        if (value == null || value.isNull()) {
            return null;
        }

        return LocalDate.parse(value.asText());
    }
}