package com.finora.receipt.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String upload(MultipartFile file);

    void delete(String storageKey);

}
