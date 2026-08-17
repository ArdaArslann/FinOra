package com.finora.receipt.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import com.finora.receipt.service.ReceiptExtractionResult;
import com.finora.receipt.service.ReceiptExtractor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@Primary
public class GeminiVisionReceiptExtractor
                implements ReceiptExtractor {

        private final Client client;
        private final ObjectMapper objectMapper;
        private final String model;

        public GeminiVisionReceiptExtractor(
                        ObjectMapper objectMapper,
                        @Value("${gemini.api-key}") String apiKey,
                        @Value("${gemini.model}") String model) {
                this.objectMapper = objectMapper;
                this.model = model;

                this.client = Client.builder()
                                .apiKey(apiKey)
                                .build();
        }

        @Override
        public ReceiptExtractionResult extract(
                        byte[] imageData,
                        String contentType) {

                if (imageData == null
                                || imageData.length == 0) {

                        throw new IllegalArgumentException(
                                        "Image data cannot be empty.");
                }

                long start = System.currentTimeMillis();

                try {

                        return executeRequest(
                                        imageData,
                                        contentType);

                } catch (Exception firstException) {

                        if (!isRetryable(firstException)) {

                                throw new IllegalStateException(
                                                "Gemini Vision receipt extraction failed.",
                                                firstException);
                        }

                        waitBeforeRetry();

                        try {

                                return executeRequest(
                                                imageData,
                                                contentType);

                        } catch (Exception secondException) {

                                throw new IllegalStateException(
                                                "Gemini Vision receipt extraction "
                                                                + "failed after retry.",
                                                secondException);
                        }

                } finally {

                        long elapsed = System.currentTimeMillis() - start;

                }
        }

        private ReceiptExtractionResult executeRequest(
                        byte[] imageData,
                        String contentType) {

                long requestStart = System.currentTimeMillis();

                String prompt = """
                                This is a photo of a receipt/invoice.
                                Analyze the image carefully and
                                extract the following information.

                                RULES:

                                1. merchantName — The store/business name
                                   at the top of the receipt. It might be
                                   written in large or bold text.

                                2. totalAmount — The total amount to be paid.
                                   Priority order:
                                   - "ÖDENECEK TUTAR" or "ODENECEK TUTAR"
                                   - Amount next to "NAKİT" or "KREDİ KARTI"
                                   - "GENEL TOPLAM"
                                   - "TOPLAM"
                                   - "TOTAL"
                                   Write the amount using a dot as the decimal separator.
                                   Example: 1500.00, 13.85, 250.99
                                   Do NOT use a thousand separator.

                                3. transactionDate — The date of the receipt.
                                   Write in YYYY-MM-DD format.
                                   On Turkish receipts, it might be
                                   DD.MM.YYYY or DD/MM/YYYY.

                                4. currency — The currency code.
                                   For Turkish receipts, use TRY.
                                   Others: USD, EUR, GBP, etc.
                                   If you are unsure and the receipt is in Turkish, use TRY.

                                5. suggestedCategory — The expense category.
                                   Select ONLY ONE from the following categories:
                                   Food, Transport, Shopping, Bills,
                                   Entertainment, Health, Other

                                   Food: Restaurant, cafe, grocery, meals
                                   Transport: Taxi, fuel, parking, public transit
                                   Shopping: Clothing, electronics, retail
                                   Bills: Electricity, water, internet, phone
                                   Entertainment: Cinema, gaming, concert
                                   Health: Pharmacy, hospital, doctor
                                   Other: Anything that does not fit above

                                IMPORTANT:
                                - If the information cannot be found, return null.
                                - Do not fabricate information.
                                - totalAmount must definitely be written
                                  using a dot as the decimal separator (e.g. 13.85).
                                """;

                String mimeType = contentType != null
                                ? contentType
                                : "image/jpeg";

                Content content = Content.fromParts(
                                Part.fromText(prompt),
                                Part.fromBytes(imageData, mimeType));

                GenerateContentConfig config = GenerateContentConfig.builder()
                                .responseMimeType(
                                                "application/json")
                                .responseSchema(
                                                buildResponseSchema())
                                .build();

                GenerateContentResponse response = client.models.generateContent(
                                model,
                                content,
                                config);

                long elapsed = System.currentTimeMillis()
                                - requestStart;

                return parseResponse(
                                response.text());
        }

        private ReceiptExtractionResult parseResponse(
                        String jsonText) {

                try {

                        var node = objectMapper.readTree(
                                        jsonText);

                        String merchantName = nullableText(node, "merchantName");

                        BigDecimal totalAmount = nullableDecimal(node, "totalAmount");

                        LocalDate transactionDate = nullableDate(node, "transactionDate");

                        String currency = nullableText(node, "currency");

                        String suggestedCategory = nullableText(node, "suggestedCategory");

                        return new ReceiptExtractionResult(
                                        merchantName,
                                        totalAmount,
                                        transactionDate,
                                        currency,
                                        suggestedCategory);

                } catch (Exception e) {

                        throw new IllegalStateException(
                                        "Failed to parse Gemini Vision "
                                                        + "receipt response.",
                                        e);
                }
        }

        private Schema buildResponseSchema() {

                return Schema.builder()
                                .type(Type.Known.OBJECT)
                                .properties(
                                                Map.of(
                                                                "merchantName",
                                                                Schema.builder()
                                                                                .type(Type.Known.STRING)
                                                                                .nullable(true)
                                                                                .build(),

                                                                "totalAmount",
                                                                Schema.builder()
                                                                                .type(Type.Known.NUMBER)
                                                                                .nullable(true)
                                                                                .build(),

                                                                "transactionDate",
                                                                Schema.builder()
                                                                                .type(Type.Known.STRING)
                                                                                .nullable(true)
                                                                                .build(),

                                                                "currency",
                                                                Schema.builder()
                                                                                .type(Type.Known.STRING)
                                                                                .nullable(true)
                                                                                .build(),

                                                                "suggestedCategory",
                                                                Schema.builder()
                                                                                .type(Type.Known.STRING)
                                                                                .enum_(
                                                                                                List.of(
                                                                                                                "Food",
                                                                                                                "Transport",
                                                                                                                "Shopping",
                                                                                                                "Bills",
                                                                                                                "Entertainment",
                                                                                                                "Health",
                                                                                                                "Other"))
                                                                                .build()))
                                .required(
                                                List.of(
                                                                "merchantName",
                                                                "totalAmount",
                                                                "transactionDate",
                                                                "currency",
                                                                "suggestedCategory"))
                                .build();
        }

        private String nullableText(
                        com.fasterxml.jackson.databind.JsonNode node,
                        String field) {

                var value = node.get(field);

                if (value == null || value.isNull()) {
                        return null;
                }

                String text = value.asText();

                return text.isBlank() ? null : text;
        }

        private BigDecimal nullableDecimal(
                        com.fasterxml.jackson.databind.JsonNode node,
                        String field) {

                var value = node.get(field);

                if (value == null || value.isNull()) {
                        return null;
                }

                if (value.isNumber()) {
                        return value.decimalValue();
                }

                String text = value.asText();

                if (text.isBlank()) {
                        return null;
                }

                /*
                 * Gemini sometimes returns in Turkish
                 * format (1.500,00).
                 * Handle this.
                 */
                if (text.contains(",")) {
                        text = text
                                        .replace(".", "")
                                        .replace(",", ".");
                }

                return new BigDecimal(text);
        }

        private LocalDate nullableDate(
                        com.fasterxml.jackson.databind.JsonNode node,
                        String field) {

                var value = node.get(field);

                if (value == null || value.isNull()) {
                        return null;
                }

                String text = value.asText();

                if (text.isBlank()) {
                        return null;
                }

                return LocalDate.parse(text);
        }

        private boolean isRetryable(
                        Exception exception) {

                String message = exception.getMessage();

                if (message == null) {
                        return true;
                }

                String lower = message.toLowerCase();

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

                        throw new IllegalStateException(
                                        "Gemini Vision retry was interrupted.",
                                        e);
                }
        }
}
