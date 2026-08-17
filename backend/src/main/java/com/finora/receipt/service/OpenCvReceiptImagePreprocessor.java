package com.finora.receipt.service;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OpenCvReceiptImagePreprocessor
        implements ReceiptImagePreprocessor {

    private static final int TARGET_WIDTH_MIN = 1500;
    private static final int TARGET_WIDTH_MAX = 3000;
    private static final int OPTIMAL_WIDTH = 2400;

    @Override
    public byte[] preprocess(
            byte[] file,
            String contentType
    ) {

        if (file == null || file.length == 0) {
            throw new IllegalArgumentException(
                    "Image file is empty."
            );
        }

        Mat original = Imgcodecs.imdecode(
                new MatOfByte(file),
                Imgcodecs.IMREAD_COLOR
        );

        if (original.empty()) {
            throw new IllegalArgumentException(
                    "Failed to decode image."
            );
        }

        List<Mat> toRelease = new ArrayList<>();
        toRelease.add(original);

        try {

            /*
             * 1. AKILLI RESIZE
             *
             * Hem çok küçük hem çok büyük
             * görselleri optimal aralığa getir.
             * Tesseract 300 DPI civarında
             * en iyi çalışır.
             */
            Mat resized =
                    smartResize(original);

            if (resized != original) {
                toRelease.add(resized);
            }

            /*
             * 2. GRAYSCALE DÖNÜŞÜM
             */
            Mat gray = new Mat();
            toRelease.add(gray);

            Imgproc.cvtColor(
                    resized,
                    gray,
                    Imgproc.COLOR_BGR2GRAY
            );

            /*
             * 3. CLAHE
             *
             * Düşük kontrastlı fişleri iyileştir.
             * clipLimit=2.0 aşırı kontrast
             * artışını engeller.
             */
            Mat clahe = new Mat();
            toRelease.add(clahe);

            applyClahe(gray, clahe);

            /*
             * 4. BILATERAL FILTER
             *
             * Kenarları koruyarak gürültü gider.
             * GaussianBlur yerine kullanıyoruz
             * çünkü metin kenarlarını bulanıklaştırmaz.
             */
            Mat filtered = new Mat();
            toRelease.add(filtered);

            Imgproc.bilateralFilter(
                    clahe,
                    filtered,
                    9,
                    75,
                    75
            );

            /*
             * 5. ADAPTIVE THRESHOLD
             *
             * blockSize=21, C=8 fiş metinleri
             * için optimize edildi.
             * Önceki değerler (31, 11) çok
             * agresifti ve ince yazıları
             * yok ediyordu.
             */
            Mat thresholded = new Mat();
            toRelease.add(thresholded);

            Imgproc.adaptiveThreshold(
                    filtered,
                    thresholded,
                    255,
                    Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                    Imgproc.THRESH_BINARY,
                    21,
                    8
            );

            /*
             * 6. MORFOLOJİK TEMİZLİK
             *
             * Küçük gürültü noktalarını temizle.
             * MORPH_OPEN: küçük beyaz noktaları sil
             * MORPH_CLOSE: küçük siyah boşlukları doldur
             */
            Mat cleaned = new Mat();
            toRelease.add(cleaned);

            Mat kernel = Imgproc.getStructuringElement(
                    Imgproc.MORPH_RECT,
                    new Size(2, 2)
            );
            toRelease.add(kernel);

            Imgproc.morphologyEx(
                    thresholded,
                    cleaned,
                    Imgproc.MORPH_CLOSE,
                    kernel
            );

            /*
             * 7. DESKEW (EĞRİLİK DÜZELTME)
             *
             * Eğik çekilmiş fişleri düzelt.
             * Küçük açılar için (< 15 derece)
             * düzeltme yapar.
             */
            Mat deskewed = deskew(cleaned);

            if (deskewed != cleaned) {
                toRelease.add(deskewed);
            }

            /*
             * 8. ENCODE
             */
            MatOfByte output = new MatOfByte();
            toRelease.add(output);

            boolean encoded =
                    Imgcodecs.imencode(
                            ".png",
                            deskewed,
                            output
                    );

            if (!encoded) {
                throw new IllegalStateException(
                        "Failed to encode preprocessed image."
                );
            }

            return output.toArray();

        } finally {

            for (Mat mat : toRelease) {
                if (mat != null) {
                    mat.release();
                }
            }
        }
    }

    /**
     * Hem çok küçük hem çok büyük görselleri
     * optimal aralığa getir.
     *
     * < 1500px → büyüt (INTER_CUBIC)
     * > 3000px → küçült (INTER_AREA)
     * Arada → olduğu gibi bırak
     */
    private Mat smartResize(Mat image) {

        int width = image.width();

        if (width >= TARGET_WIDTH_MIN
                && width <= TARGET_WIDTH_MAX) {

            return image;
        }

        int targetWidth;
        int interpolation;

        if (width < TARGET_WIDTH_MIN) {

            targetWidth = OPTIMAL_WIDTH;
            interpolation = Imgproc.INTER_CUBIC;

        } else {

            targetWidth = OPTIMAL_WIDTH;
            interpolation = Imgproc.INTER_AREA;
        }

        double scale =
                (double) targetWidth / width;

        int targetHeight =
                (int) (image.height() * scale);

        Mat resized = new Mat();

        Imgproc.resize(
                image,
                resized,
                new Size(
                        targetWidth,
                        targetHeight
                ),
                0,
                0,
                interpolation
        );

        return resized;
    }

    /**
     * CLAHE (Contrast Limited Adaptive
     * Histogram Equalization)
     *
     * Düşük kontrastlı fişleri iyileştirir.
     * clipLimit=2.0 aşırı kontrast artışını
     * engeller. tileGridSize=8x8 lokal kontrast
     * iyileştirmesi yapar.
     */
    private void applyClahe(
            Mat input,
            Mat output
    ) {

        var clahe = Imgproc.createCLAHE(
                2.0,
                new Size(8, 8)
        );

        clahe.apply(input, output);
    }

    /**
     * Eğik fişleri düzeltme.
     *
     * minAreaRect ile baskın açıyı bulur
     * ve küçük açılar (< 15°) için
     * düzeltme uygular.
     */
    private Mat deskew(Mat image) {

        Mat inverted = new Mat();
        Core.bitwise_not(image, inverted);

        List<MatOfPoint> contours =
                new ArrayList<>();

        Mat hierarchy = new Mat();

        Imgproc.findContours(
                inverted,
                contours,
                hierarchy,
                Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_SIMPLE
        );

        inverted.release();
        hierarchy.release();

        if (contours.isEmpty()) {
            return image;
        }

        /*
         * Tüm konturları birleştirip
         * baskın açıyı bul.
         */
        MatOfPoint allPoints = new MatOfPoint();
        List<Point> allPointsList =
                new ArrayList<>();

        for (MatOfPoint contour : contours) {

            allPointsList.addAll(
                    contour.toList()
            );

            contour.release();
        }

        allPoints.fromList(allPointsList);

        MatOfPoint2f points2f = new MatOfPoint2f(
                allPoints.toArray()
        );

        allPoints.release();

        RotatedRect rotatedRect =
                Imgproc.minAreaRect(points2f);

        points2f.release();

        double angle = rotatedRect.angle;

        /*
         * minAreaRect açısı -90 ile 0 arasında.
         * Küçük düzeltmeler için:
         *  angle < -45 → angle += 90
         *  Sonuç |angle| < 15 ise düzelt.
         */
        if (angle < -45.0) {
            angle += 90.0;
        }

        if (Math.abs(angle) < 0.5
                || Math.abs(angle) > 15.0) {

            return image;
        }

        Point center = new Point(
                image.width() / 2.0,
                image.height() / 2.0
        );

        Mat rotationMatrix =
                Imgproc.getRotationMatrix2D(
                        center,
                        angle,
                        1.0
                );

        Mat rotated = new Mat();

        Imgproc.warpAffine(
                image,
                rotated,
                rotationMatrix,
                image.size(),
                Imgproc.INTER_CUBIC,
                Core.BORDER_REPLICATE
        );

        rotationMatrix.release();

        return rotated;
    }
}