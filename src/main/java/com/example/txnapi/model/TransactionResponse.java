package com.example.txnapi.model;

public class TransactionResponse {

    private boolean success;
    private String transactionId;
    private String fileName;
    private String message;

    public TransactionResponse() {
    }

    public TransactionResponse(boolean success, String transactionId, String fileName, String message) {
        this.success = success;
        this.transactionId = transactionId;
        this.fileName = fileName;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
