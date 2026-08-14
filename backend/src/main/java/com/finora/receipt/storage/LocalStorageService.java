package com.finora.receipt.storage;

import com.finora.receipt.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    private final Path uploadDirectory;

    public LocalStorageService(
            @Value("${finora.storage.upload-dir:uploads/receipts}")
            String uploadDir
    ) {
        this.uploadDirectory = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();
    }

    @Override
    public String upload(MultipartFile file) {

        try {
            Files.createDirectories(uploadDirectory);

            String extension = getExtension(file.getOriginalFilename());

            String storageKey = UUID.randomUUID() + extension;

            Path target = uploadDirectory.resolve(storageKey);

            file.transferTo(target);

            return storageKey;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to store receipt file.",
                    e
            );
        }
    }

    @Override
    public byte[] download(String storageKey) {

        try {
            Path target = uploadDirectory
                    .resolve(storageKey)
                    .normalize();

            if (!target.startsWith(uploadDirectory)) {
                throw new IllegalArgumentException(
                        "Invalid storage key."
                );
            }

            if (!Files.exists(target)) {
                throw new IllegalArgumentException(
                        "Receipt file not found."
                );
            }

            return Files.readAllBytes(target);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to read receipt file.",
                    e
            );
        }
    }


    @Override
    public void delete(String storageKey) {

        try {
            Path target = uploadDirectory
                    .resolve(storageKey)
                    .normalize();

            if (!target.startsWith(uploadDirectory)) {
                throw new IllegalArgumentException(
                        "Invalid storage key."
                );
            }

            Files.deleteIfExists(target);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to delete receipt file.",
                    e
            );
        }
    }

    private String getExtension(String fileName) {

        if (fileName == null || !fileName.contains(".")) {
            return "";
        }

        return fileName.substring(
                fileName.lastIndexOf(".")
        );
    }
}