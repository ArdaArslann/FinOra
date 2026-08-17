package com.finora.receipt.service;

import com.finora.receipt.entity.ReceiptEntity;
import com.finora.receipt.entity.ReceiptExtractionEntity;
import com.finora.receipt.enums.ReceiptStatus;
import com.finora.receipt.repository.ReceiptExtractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReceiptExtractionServiceImpl
        implements ReceiptExtractionService {

    private final ReceiptExtractionRepository extractionRepository;

    private final ReceiptExtractor receiptExtractor;

    private final StorageService storageService;

    @Override
    public void createExtraction(
            ReceiptEntity receipt
    ) {

        ReceiptExtractionEntity extraction =
                ReceiptExtractionEntity.create(
                        receipt
                );

        receipt.assignExtraction(
                extraction
        );

        extractionRepository.save(
                extraction
        );

        receipt.updateStatus(
                ReceiptStatus.PROCESSING
        );

        try {

            long start =
                    System.currentTimeMillis();

            /*
             * 1. FOTOĞRAFI STORAGE'DAN AL
             */
            byte[] file =
                    storageService.download(
                            receipt.getStorageKey()
                    );

            long afterDownload =
                    System.currentTimeMillis();

            /*
             * 2. GEMINI VISION İLE TÜM BİLGİLERİ ÇIKAR
             *
             * Doğrudan görüntüyü gönderiyoruz.
             * Gemini Vision API tek adımda:
             * - merchantName
             * - totalAmount
             * - transactionDate
             * - currency
             * - suggestedCategory
             * çıkarıyor.
             */
            ReceiptExtractionResult result =
                    receiptExtractor.extract(
                            file,
                            receipt.getContentType()
                    );

            long afterExtraction =
                    System.currentTimeMillis();

            /*
             * 3. SONUÇLARI KAYDET
             */
            extraction.updateExtraction(

                    result.merchantName(),

                    result.totalAmount(),

                    result.transactionDate(),

                    result.currency(),

                    result.suggestedCategory()
            );

            receipt.updateStatus(
                    ReceiptStatus.PROCESSED
            );

            System.out.println(
                    "========== RECEIPT EXTRACTION =========="
            );

            System.out.println(
                    "TOTAL = " +
                            result.totalAmount()
            );

            System.out.println(
                    "MERCHANT = " +
                            result.merchantName()
            );

            System.out.println(
                    "DATE = " +
                            result.transactionDate()
            );

            System.out.println(
                    "CURRENCY = " +
                            result.currency()
            );

            System.out.println(
                    "CATEGORY = " +
                            result.suggestedCategory()
            );

            System.out.println(
                    "========================================="
            );

            System.out.println(
                    "DOWNLOAD: " +
                            (afterDownload - start) +
                            " ms"
            );

            System.out.println(
                    "EXTRACTION: " +
                            (afterExtraction - afterDownload) +
                            " ms"
            );

            System.out.println(
                    "TOTAL TIME: " +
                            (afterExtraction - start) +
                            " ms"
            );

        } catch (Exception e) {

            receipt.updateStatus(
                    ReceiptStatus.FAILED
            );

            System.err.println(
                    "Receipt extraction failed: " +
                            e.getMessage()
            );

            e.printStackTrace();

            throw e;
        }
    }
}