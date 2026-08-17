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
                        ReceiptEntity receipt) {

                ReceiptExtractionEntity extraction = ReceiptExtractionEntity.create(
                                receipt);

                receipt.assignExtraction(
                                extraction);

                extractionRepository.save(
                                extraction);

                receipt.updateStatus(
                                ReceiptStatus.PROCESSING);

                try {

                        long start = System.currentTimeMillis();

                        /*
                         * 1. GET PHOTO FROM STORAGE
                         */
                        byte[] file = storageService.download(
                                        receipt.getStorageKey());

                        long afterDownload = System.currentTimeMillis();

                        /*
                         * 2. EXTRACT ALL INFO WITH GEMINI VISION
                         *
                         * We send the image directly.
                         * Gemini Vision API extracts in one step:
                         * - merchantName
                         * - totalAmount
                         * - transactionDate
                         * - currency
                         * - suggestedCategory
                         */
                        ReceiptExtractionResult result = receiptExtractor.extract(
                                        file,
                                        receipt.getContentType());

                        long afterExtraction = System.currentTimeMillis();

                        /*
                         * 3. SAVE RESULTS
                         */
                        extraction.updateExtraction(

                                        result.merchantName(),

                                        result.totalAmount(),

                                        result.transactionDate(),

                                        result.currency(),

                                        result.suggestedCategory());

                        receipt.updateStatus(
                                        ReceiptStatus.PROCESSED);

                } catch (Exception e) {

                        receipt.updateStatus(
                                        ReceiptStatus.FAILED);

                        e.printStackTrace();

                        throw e;
                }
        }
}