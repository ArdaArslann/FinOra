package com.finora.test;

import com.finora.receipt.dto.ReceiptExtractionResponse;
import com.finora.receipt.service.ReceiptExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final ReceiptExtractor receiptExtractor;

    @GetMapping
    public String test() {
        return "authenticated";
    }

    @PostMapping(
            value = "/ocr",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ReceiptExtractionResponse testOcr(
            @RequestParam("file") MultipartFile file
    ) {

        try {

            /*
             * GEMINI VISION → TÜM BİLGİLER
             *
             * Doğrudan görüntüyü gönderiyoruz.
             * Tek adımda merchant, total, date,
             * currency ve category çıkıyor.
             */
            var result =
                    receiptExtractor.extract(
                            file.getBytes(),
                            file.getContentType()
                    );

            String currency =
                    result.currency() != null
                            ? result.currency()
                            : "TRY";

            return new ReceiptExtractionResponse(

                    result.merchantName(),

                    result.totalAmount(),

                    result.transactionDate(),

                    currency,

                    result.suggestedCategory()
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "OCR test failed.",
                    e
            );
        }
    }
}