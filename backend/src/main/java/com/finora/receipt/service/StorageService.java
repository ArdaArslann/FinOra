package com.finora.receipt.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String upload(MultipartFile file);

    byte[] download(String storageKey);

    void delete(String storageKey);
}
