package com.finora.receipt.service;

import lombok.RequiredArgsConstructor;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

@Service
@RequiredArgsConstructor
public class TesseractReceiptOcrService
        implements ReceiptOcrService {

    private final ReceiptImagePreprocessor imagePreprocessor;

    @Override
    public String extractText(
            byte[] file,
            String contentType
    ) {

        try {

            byte[] processedFile =
                    imagePreprocessor.preprocess(
                            file,
                            contentType
                    );

            BufferedImage image =
                    ImageIO.read(
                            new ByteArrayInputStream(
                                    processedFile
                            )
                    );

            if (image == null) {
                throw new IllegalArgumentException(
                        "Failed to decode preprocessed image."
                );
            }

            ITesseract tesseract =
                    new Tesseract();

            tesseract.setDatapath(
                    "/usr/share/tesseract-ocr/5/tessdata"
            );

            /*
             * Dil: Türkçe + İngilizce
             */
            tesseract.setLanguage("tur+eng");

            /*
             * OEM 1: Sadece LSTM engine.
             * LSTM en doğru sonuçları verir.
             * OEM 3 (LSTM + Legacy) bazı
             * ayarlarla çakışabiliyor.
             */
            tesseract.setOcrEngineMode(1);

            /*
             * PSM 4: Tek sütun, değişken boyutlu metin.
             * Fişler genellikle tek sütun yapıdadır.
             * PSM 6 (uniform block) fişlerdeki farklı
             * font boyutlarını düzgün işleyemez.
             */
            tesseract.setPageSegMode(4);

            /*
             * DPI 300: Tesseract 300 DPI'da
             * en iyi performansı gösterir.
             * Preprocessing zaten görseli
             * uygun boyuta getiriyor.
             */
            tesseract.setVariable(
                    "user_defined_dpi",
                    "300"
            );

            /*
             * Kelimeler arası boşlukları koru.
             * Fişlerde fiyat ve açıklama arasındaki
             * boşluklar önemli.
             */
            tesseract.setVariable(
                    "preserve_interword_spaces",
                    "1"
            );

            String result =
                    tesseract.doOCR(image);

            System.out.println(
                    "========== RAW OCR OUTPUT =========="
            );

            System.out.println(result);

            System.out.println(
                    "===================================="
            );

            return result;

        } catch (Exception e) {

            throw new IllegalStateException(
                    "OCR failed.",
                    e
            );
        }
    }
}