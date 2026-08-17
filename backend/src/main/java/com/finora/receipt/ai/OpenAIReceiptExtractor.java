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
import java.util.List;
import java.util.Map;

@Service
public class OpenAIReceiptExtractor
        implements ReceiptExtractor {

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

        this.restClient =
                RestClient.builder()
                        .baseUrl(
                                "https://api.openai.com/v1"
                        )
                        .defaultHeader(
                                "Authorization",
                                "Bearer " + apiKey
                        )
                        .build();
    }

    @Override
    public ReceiptExtractionResult extract(
            byte[] imageData,
            String contentType
    ) {

        if (imageData == null
                || imageData.length == 0) {

            throw new IllegalArgumentException(
                    "Image data cannot be empty."
            );
        }

        Map<String, Object> request =
                buildRequest(imageData, contentType);

        String response =
                restClient.post()
                        .uri("/responses")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .body(request)
                        .retrieve()
                        .body(String.class);

        return parseResponse(response);
    }

    private Map<String, Object> buildRequest(
            byte[] imageData,
            String contentType
    ) {

        String prompt = """
                Analyze this receipt image.

                Extract:

                1. merchantName — Business name at
                   the top of the receipt.

                2. totalAmount — Final payable amount.
                   Use dot as decimal separator.
                   Example: 1500.00, 13.85
                   Do NOT use thousand separators.

                3. transactionDate — Date in YYYY-MM-DD.

                4. currency — Currency code
                   (TRY, USD, EUR, etc).

                5. suggestedCategory — One of:
                   Food, Transport, Shopping, Bills,
                   Entertainment, Health, Other

                Return null for unknown fields.
                Do not invent information.
                """;

        String base64Image =
                Base64.getEncoder()
                        .encodeToString(imageData);

        String mimeType =
                contentType != null
                        ? contentType
                        : "image/jpeg";

        Map<String, Object> content =
                Map.of(
                        "role",
                        "user",

                        "content",
                        List.of(
                                Map.of(
                                        "type",
                                        "input_text",

                                        "text",
                                        prompt
                                ),
                                Map.of(
                                        "type",
                                        "input_image",

                                        "image_url",
                                        "data:" + mimeType
                                                + ";base64,"
                                                + base64Image
                                )
                        )
                );

        Map<String, Object> schema =
                Map.of(
                        "type",
                        "object",

                        "properties",
                        Map.of(

                                "merchantName",
                                Map.of(
                                        "type",
                                        List.of(
                                                "string",
                                                "null"
                                        )
                                ),

                                "totalAmount",
                                Map.of(
                                        "type",
                                        List.of(
                                                "number",
                                                "null"
                                        )
                                ),

                                "transactionDate",
                                Map.of(
                                        "type",
                                        List.of(
                                                "string",
                                                "null"
                                        )
                                ),

                                "currency",
                                Map.of(
                                        "type",
                                        List.of(
                                                "string",
                                                "null"
                                        )
                                ),

                                "suggestedCategory",
                                Map.of(
                                        "type",
                                        "string",
                                        "enum",
                                        List.of(
                                                "Food",
                                                "Transport",
                                                "Shopping",
                                                "Bills",
                                                "Entertainment",
                                                "Health",
                                                "Other"
                                        )
                                )
                        ),

                        "required",
                        List.of(
                                "merchantName",
                                "totalAmount",
                                "transactionDate",
                                "currency",
                                "suggestedCategory"
                        ),

                        "additionalProperties",
                        false
                );

        Map<String, Object> format =
                Map.of(
                        "type",
                        "json_schema",

                        "name",
                        "receipt_extraction",

                        "strict",
                        true,

                        "schema",
                        schema
                );

        return Map.of(
                "model",
                model,

                "input",
                List.of(content),

                "text",
                Map.of(
                        "format",
                        format
                )
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
                    objectMapper.readTree(
                            jsonText
                    );

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
                    "Failed to parse OpenAI receipt response.",
                    e
            );
        }
    }

    private String findOutputText(
            JsonNode root
    ) {

        for (JsonNode item :
                root.path("output")) {

            for (JsonNode content :
                    item.path("content")) {

                if ("output_text".equals(
                        content
                                .path("type")
                                .asText()
                )) {

                    return content
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

        JsonNode value =
                node.get(field);

        if (value == null ||
                value.isNull()) {

            return null;
        }

        String text =
                value.asText();

        return text.isBlank()
                ? null
                : text;
    }

    private BigDecimal nullableDecimal(
            JsonNode node,
            String field
    ) {

        JsonNode value =
                node.get(field);

        if (value == null ||
                value.isNull()) {

            return null;
        }

        if (value.isNumber()) {
            return value.decimalValue();
        }

        String text =
                value.asText();

        if (text.isBlank()) {
            return null;
        }

        return new BigDecimal(text);
    }

    private LocalDate nullableDate(
            JsonNode node,
            String field
    ) {

        JsonNode value =
                node.get(field);

        if (value == null ||
                value.isNull()) {

            return null;
        }

        String text =
                value.asText();

        if (text.isBlank()) {
            return null;
        }

        return LocalDate.parse(text);
    }
}