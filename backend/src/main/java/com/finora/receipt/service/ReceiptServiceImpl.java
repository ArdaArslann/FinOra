package com.finora.receipt.service;

import com.finora.common.exception.BusinessException;
import com.finora.common.exception.ResourceNotFoundException;
import com.finora.common.security.CurrentUserService;
import com.finora.receipt.dto.ConfirmReceiptRequest;
import com.finora.receipt.dto.ReceiptResponse;
import com.finora.receipt.entity.ReceiptEntity;
import com.finora.receipt.enums.ReceiptStatus;
import com.finora.receipt.mapper.ReceiptMapper;
import com.finora.receipt.repository.ReceiptRepository;
import com.finora.transaction.dto.CreateTransactionRequest;
import com.finora.transaction.dto.TransactionResponse;
import com.finora.transaction.entity.TransactionEntity;
import com.finora.transaction.enums.TransactionType;
import com.finora.transaction.service.TransactionService;
import com.finora.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.Set;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReceiptServiceImpl implements ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final ReceiptMapper receiptMapper;
    private final CurrentUserService currentUserService;
    private final StorageService storageService;
    private final ReceiptExtractionService receiptExtractionService;
    private final TransactionService transactionService;

    @Override
    public ReceiptResponse upload(MultipartFile file) {

        validateFile(file);

        UserEntity currentUser =
                currentUserService.getCurrentUser();

        String storageKey =
                storageService.upload(file);

        ReceiptEntity receipt =
                ReceiptEntity.create(
                        file.getOriginalFilename(),
                        storageKey,
                        file.getContentType(),
                        file.getSize(),
                        currentUser
                );

        receipt = receiptRepository.save(receipt);

        receiptExtractionService.createExtraction(receipt);

        return receiptMapper.toResponse(receipt);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReceiptResponse> getAll() {

        UserEntity currentUser =
                currentUserService.getCurrentUser();

        return receiptRepository
                .findAllByUserOrderByUploadedAtDesc(currentUser)
                .stream()
                .map(receiptMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiptResponse getById(UUID id) {

        UserEntity currentUser =
                currentUserService.getCurrentUser();

        ReceiptEntity receipt =
                getReceiptOrThrow(id, currentUser);

        return receiptMapper.toResponse(receipt);
    }

    @Override
    public void delete(UUID id) {

        UserEntity currentUser =
                currentUserService.getCurrentUser();

        ReceiptEntity receipt =
                getReceiptOrThrow(id, currentUser);

        storageService.delete(
                receipt.getStorageKey()
        );

        receiptRepository.delete(receipt);
    }

    @Override
    public TransactionResponse confirmReceipt(
            UUID id,
            ConfirmReceiptRequest request
    ) {

        UserEntity currentUser =
                currentUserService.getCurrentUser();

        ReceiptEntity receipt =
                getReceiptOrThrow(id, currentUser);

        if (receipt.getStatus() != ReceiptStatus.PROCESSED) {
            throw new BusinessException(
                    "RECEIPT_NOT_PROCESSED",
                    "Receipt must be processed before confirmation."
            );
        }

        if (receipt.getExtraction() == null) {
            throw new BusinessException(
                    "RECEIPT_EXTRACTION_NOT_FOUND",
                    "Receipt extraction not found."
            );
        }

        if (receipt.getTransaction() != null) {
            throw new BusinessException(
                    "RECEIPT_ALREADY_CONFIRMED",
                    "Receipt is already linked to a transaction."
            );
        }

        CreateTransactionRequest transactionRequest =
                new CreateTransactionRequest(
                        request.amount(),
                        TransactionType.EXPENSE,
                        request.description(),
                        request.transactionDate(),
                        request.categoryId(),
                        receipt.getId()
                );

        return transactionService.createTransaction(
                transactionRequest
        );
    }

    private ReceiptEntity getReceiptOrThrow(
            UUID id,
            UserEntity user
    ) {

        return receiptRepository
                .findByIdAndUser(id, user)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "RECEIPT_NOT_FOUND",
                                "Receipt not found"
                        )
                );
    }

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png",
                    "application/pdf"
            );

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(
                    "jpg",
                    "jpeg",
                    "png",
                    "pdf"
            );

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BusinessException(
                    "EMPTY_RECEIPT_FILE",
                    "Receipt file cannot be empty."
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(
                    "RECEIPT_FILE_TOO_LARGE",
                    "Receipt file size cannot exceed 5 MB."
            );
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {

            throw new BusinessException(
                    "INVALID_RECEIPT_FILE_TYPE",
                    "Only JPG, PNG and PDF files are allowed."
            );
        }

        String extension = getFileExtension(
                file.getOriginalFilename()
        );

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(
                    "INVALID_RECEIPT_FILE_EXTENSION",
                    "Only JPG, PNG and PDF files are allowed."
            );
        }
    }

    private String getFileExtension(String fileName) {

        if (fileName == null || !fileName.contains(".")) {
            return "";
        }

        return fileName
                .substring(fileName.lastIndexOf('.') + 1)
                .toLowerCase();
    }

}