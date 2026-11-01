package com.example.demo.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PharmacyBusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(PharmacyBusinessException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("errorCode", ex.getErrorCode());
        body.put("error", "[" + ex.getErrorCode() + "] " + ex.getMessage());
        body.put("message", ex.getMessage());
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        String code = "E60";
        if (msg != null && msg.contains("Insufficient stock for this batch")) {
            code = "E33";
        } else if (msg != null && msg.contains("foreign key")) {
            code = "E60";
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("errorCode", code);
        body.put("error", "[" + code + "] Database Integrity Constraint: " + msg);
        body.put("message", msg);
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Map<String, Object>> handleSQLException(SQLException ex) {
        String code = "E61";
        if ("45000".equals(ex.getSQLState())) {
            code = "E33";
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("errorCode", code);
        body.put("error", "[" + code + "] SQL Error: " + ex.getMessage());
        body.put("message", ex.getMessage());
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("errorCode", "E62");
        body.put("error", "[E62] Unexpected Server Error: " + ex.getMessage());
        body.put("message", ex.getMessage());
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
