package com.example.demo.exception;

/**
 * Custom application business exception that carries a specific error code
 * (E01 - E62) according to the Aruna Pharmacies validation specification.
 */
public class PharmacyBusinessException extends RuntimeException {

    private final String errorCode;

    public PharmacyBusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "[" + errorCode + "] " + getMessage();
    }
}
