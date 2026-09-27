package com.example.txnapi.controller;

import com.example.txnapi.model.TransactionResponse;
import com.example.txnapi.service.TransactionService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * POST /api/transactions
     * multipart/form-data:
     *   file   - the file to append the transaction id to
     *   amount - the amount to transact (must be between 100 and 100000)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TransactionResponse> processTransaction(
            @RequestParam("file") MultipartFile file,
            @RequestParam("amount") BigDecimal amount) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new TransactionResponse(false, null, null, "Uploaded file is empty"));
        }

        if (!transactionService.isAmountValid(amount)) {
            return ResponseEntity.badRequest().body(new TransactionResponse(
                    false, null, null,
                    "Amount must be between Rs.100 and Rs.1,00,000 (inclusive)"));
        }

        try {
            TransactionResponse response = transactionService.processAndAppend(file, amount);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new TransactionResponse(
                    false, null, null, "Failed to process file: " + e.getMessage()));
        }
    }

    /**
     * GET /api/transactions/download/{fileName}
     * Lets you fetch the file back to verify the transaction id was appended.
     */
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            Resource resource = transactionService.loadFileAsResource(fileName);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
