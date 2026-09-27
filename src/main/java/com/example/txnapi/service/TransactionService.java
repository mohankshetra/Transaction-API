package com.example.txnapi.service;

import com.example.txnapi.model.TransactionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransactionService {

    public static final BigDecimal MIN_AMOUNT = new BigDecimal("100");
    public static final BigDecimal MAX_AMOUNT = new BigDecimal("100000");

    @Value("${transaction.storage.dir:/data/transactions}")
    private String storageDir;

    /**
     * Returns true if amount is within [100, 100000] (Rs. 100 to Rs. 1,00,000), inclusive.
     */
    public boolean isAmountValid(BigDecimal amount) {
        return amount != null
                && amount.compareTo(MIN_AMOUNT) >= 0
                && amount.compareTo(MAX_AMOUNT) <= 0;
    }

    /**
     * Saves the uploaded file to disk, appends a generated transaction id + amount + timestamp
     * line to it, and returns the result.
     */
    public TransactionResponse processAndAppend(MultipartFile file, BigDecimal amount) throws IOException {
        Path dirPath = Paths.get(storageDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        String originalName = (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank())
                ? "file.txt"
                : Paths.get(file.getOriginalFilename()).getFileName().toString();

        Path targetPath = dirPath.resolve(originalName);

        // Persist the uploaded file to the storage directory (creates it, or overwrites
        // if a file with the same name was uploaded before).
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        String transactionId = "TXN-" + UUID.randomUUID();
        String logLine = String.format(
                "%nTransactionId: %s | Amount: Rs.%s | Timestamp: %s%n",
                transactionId, amount.toPlainString(), LocalDateTime.now());

        Files.write(targetPath, logLine.getBytes(), StandardOpenOption.APPEND);

        return new TransactionResponse(
                true,
                transactionId,
                originalName,
                "Transaction successful. Transaction ID appended to file.");
    }

    /**
     * Loads a previously processed file from the storage directory so it can be downloaded
     * to verify the appended transaction id.
     */
    public Resource loadFileAsResource(String fileName) throws IOException {
        try {
            Path filePath = Paths.get(storageDir).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new IOException("File not found: " + fileName);
        } catch (MalformedURLException e) {
            throw new IOException("File not found: " + fileName, e);
        }
    }
}
