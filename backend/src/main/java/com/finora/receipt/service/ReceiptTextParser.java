package com.finora.receipt.service;

import com.finora.receipt.domain.ParsedReceipt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReceiptTextParser implements ReceiptParser {

    /**
     * Supported money formats:
     *
     * 13,85
     * 1500,00
     * 1.500,00
     * 1.250.500,99
     * 13.85
     * 1 500,00   (OCR sometimes separates with a space)
     * *13,85     (starting with an asterisk)
     */
    private static final String MONEY =
            "\\*?(?:\\d{1,3}(?:[.\\s]\\d{3})*|\\d+)[,.]\\d{2}";

    /*
     * ===== PAYABLE AMOUNT =====
     *
     * Highest priority.
     *
     * Ödenecek Tutar 1.500,00
     * ÖDENECEK TUTAR : 13,85
     *
     * OCR fault tolerance:
     * - ODÉNECEK, ÖOÉNECEK, ODENECEK
     * - Ö letter → can be O, 0
     */
    private static final Pattern PAYABLE_TOTAL_PATTERN =
            Pattern.compile(
                    "(?iu)[ÖOÓ0][Dd][Ee][Nn][Ee][CcÇç][Ee][Kk]"
                            + "\\s*[Tt][Uu][Tt][Aa][Rr]"
                            + "\\D{0,50}?(" + MONEY + ")"
            );

    /*
     * ===== CASH / CREDIT CARD =====
     *
     * Second priority.
     *
     * NAKİT          1.500,00
     * NAKIT           1.500,00
     * KREDİ KARTI    1.500,00
     * KREDI KARTI    1.500,00
     * K.KARTI        1.500,00
     */
    private static final Pattern PAYMENT_METHOD_PATTERN =
            Pattern.compile(
                    "(?iu)(?:NAK[İIi]T|KRED[İIi]\\s*KART[İIi]|K\\.?\\s*KART[İIi])"
                            + "\\D{0,50}?(" + MONEY + ")"
            );

    /*
     * ===== GRAND TOTAL =====
     *
     * Third priority.
     *
     * Genel Toplam 1.500,00
     * GENEL TOPLAM : 13,85
     *
     * OCR fault tolerance:
     * - GENEL T0PLAM, GENEL TOPIAM
     */
    private static final Pattern GRAND_TOTAL_PATTERN =
            Pattern.compile(
                    "(?iu)GENEL\\s*T[O0][Pp][Ll][Aa][Mm]"
                            + "\\D{0,50}?(" + MONEY + ")"
            );

    /*
     * ===== NET TOTAL =====
     *
     * Fourth priority.
     *
     * NET TUTAR     1.500,00
     * Net Tutar :   13,85
     */
    private static final Pattern NET_TOTAL_PATTERN =
            Pattern.compile(
                    "(?iu)NET\\s*TUTAR"
                            + "\\D{0,50}?(" + MONEY + ")"
            );

    /*
     * ===== AMOUNT =====
     *
     * Fifth priority.
     *
     * TUTAR         1.500,00
     * Tutar :       13,85
     *
     * "Alt Tutar" and "Ödenecek Tutar"
     * are excluded.
     */
    private static final Pattern TUTAR_PATTERN =
            Pattern.compile(
                    "(?iu)(?<!ALT\\s)(?<![ÖOÓ0]DENECEK\\s)"
                            + "TUTAR"
                            + "\\D{0,50}?(" + MONEY + ")"
            );

    /*
     * ===== TOTAL =====
     *
     * Sixth priority.
     *
     * TOPLAM *13,85
     * TOPLAM 13,85
     * TOPLAM : 13,85
     * T0PLAM  13,85
     * TOPIAM  13,85
     *
     * "Alt Toplam" and "Genel Toplam"
     * are excluded.
     *
     * Last match is used because
     * the final total is usually
     * at the bottom of the receipt.
     */
    private static final Pattern TOTAL_PATTERN =
            Pattern.compile(
                    "(?iu)(?<!ALT\\s)(?<!GENEL\\s)"
                            + "T[O0][Pp][Ll][Aa][Mm]"
                            + "\\D{0,50}?(" + MONEY + ")"
            );

    /*
     * ===== TOTAL (English) =====
     *
     * Seventh priority.
     *
     * TOTAL    13.85
     * Total :  13.85
     * AMOUNT   13.85
     *
     * "Subtotal" is excluded.
     */
    private static final Pattern TOTAL_EN_PATTERN =
            Pattern.compile(
                    "(?iu)(?<!SUB\\s?)TOTAL"
                            + "\\D{0,50}?(" + MONEY + ")"
            );

    private static final Pattern AMOUNT_EN_PATTERN =
            Pattern.compile(
                    "(?iu)(?:AMOUNT|DUE)"
                            + "\\D{0,50}?(" + MONEY + ")"
            );

    @Override
    public ParsedReceipt parse(String ocrText) {

        if (ocrText == null || ocrText.isBlank()) {
            throw new IllegalArgumentException(
                    "OCR text cannot be empty."
            );
        }

        System.out.println(
                "========== OCR TEXT FOR PARSER =========="
        );

        System.out.println(ocrText);

        System.out.println(
                "=========================================="
        );

        BigDecimal total = extractTotal(ocrText);

        System.out.println(
                "========== PARSED RECEIPT =========="
        );

        System.out.println(
                "TOTAL = " + total
        );

        System.out.println(
                "===================================="
        );

        return new ParsedReceipt(total);
    }

    private BigDecimal extractTotal(String text) {

        BigDecimal total;

        /*
         * 1. PAYABLE AMOUNT — Most reliable
         */
        total = findMoney(
                PAYABLE_TOTAL_PATTERN,
                text,
                false
        );

        if (total != null) {

            System.out.println(
                    "TOTAL TYPE = ÖDENECEK TUTAR"
            );

            return total;
        }

        /*
         * 2. CASH / CREDIT CARD
         */
        total = findMoney(
                PAYMENT_METHOD_PATTERN,
                text,
                false
        );

        if (total != null) {

            System.out.println(
                    "TOTAL TYPE = NAKİT/KREDİ KARTI"
            );

            return total;
        }

        /*
         * 3. GRAND TOTAL
         */
        total = findMoney(
                GRAND_TOTAL_PATTERN,
                text,
                false
        );

        if (total != null) {

            System.out.println(
                    "TOTAL TYPE = GENEL TOPLAM"
            );

            return total;
        }

        /*
         * 4. NET TOTAL
         */
        total = findMoney(
                NET_TOTAL_PATTERN,
                text,
                false
        );

        if (total != null) {

            System.out.println(
                    "TOTAL TYPE = NET TUTAR"
            );

            return total;
        }

        /*
         * 5. AMOUNT (bare)
         */
        total = findMoney(
                TUTAR_PATTERN,
                text,
                false
        );

        if (total != null) {

            System.out.println(
                    "TOTAL TYPE = TUTAR"
            );

            return total;
        }

        /*
         * 6. TOTAL — Last match
         */
        total = findMoney(
                TOTAL_PATTERN,
                text,
                true
        );

        if (total != null) {

            System.out.println(
                    "TOTAL TYPE = TOPLAM"
            );

            return total;
        }

        /*
         * 7. TOTAL (English)
         */
        total = findMoney(
                TOTAL_EN_PATTERN,
                text,
                true
        );

        if (total != null) {

            System.out.println(
                    "TOTAL TYPE = TOTAL (EN)"
            );

            return total;
        }

        /*
         * 8. AMOUNT / DUE (English)
         */
        total = findMoney(
                AMOUNT_EN_PATTERN,
                text,
                false
        );

        if (total != null) {

            System.out.println(
                    "TOTAL TYPE = AMOUNT/DUE (EN)"
            );

            return total;
        }

        System.out.println(
                "TOTAL NOT FOUND"
        );

        return null;
    }

    /**
     * Find the amount matching the pattern.
     *
     * Uses the last match when useLast=true.
     * Since the final total on receipts is usually
     * at the bottom, the last match is preferred
     * for the TOTAL pattern.
     */
    private BigDecimal findMoney(
            Pattern pattern,
            String text,
            boolean useLast
    ) {

        Matcher matcher =
                pattern.matcher(text);

        String lastMatch = null;

        if (useLast) {

            while (matcher.find()) {
                lastMatch = matcher.group(1);
            }

            if (lastMatch == null) {
                return null;
            }

        } else {

            if (!matcher.find()) {
                return null;
            }

            lastMatch = matcher.group(1);
        }

        System.out.println(
                "TOTAL MATCH = [" + lastMatch + "]"
        );

        return parseTurkishMoney(lastMatch);
    }

    private BigDecimal parseTurkishMoney(
            String value
    ) {

        value = value.trim();

        /*
         * Clean asterisk (like *13,85 in receipts)
         */
        if (value.startsWith("*")) {
            value = value.substring(1);
        }

        /*
         * Clean spaces added by OCR
         * "1 500,00" → "1500,00"
         */
        value = value.replaceAll("\\s+", "");

        /*
         * Turkish format:
         *
         * 1.500,00
         *
         * "." = thousands separator
         * "," = decimal separator
         */
        if (value.contains(",")) {

            return new BigDecimal(
                    value
                            .replace(".", "")
                            .replace(",", ".")
            );
        }

        /*
         * Fallback:
         *
         * 13.85
         */
        return new BigDecimal(value);
    }
}