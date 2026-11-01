package com.example.demo.controller;

import com.example.demo.dto.InvoiceLookupDTO;
import com.example.demo.dto.SalesReturnRequestDTO;
import com.example.demo.dto.SalesReturnResponseDTO;
import com.example.demo.service.SalesReturnService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for the Returns & Refunds workflow.
 *
 * Endpoints:
 *   GET  /api/returns/invoice/{invoiceId}  → fetch invoice details for return verification
 *   POST /api/returns                      → process a sales return
 *   GET  /api/returns/history              → list past returns with items and refund totals
 */
@RestController
@RequestMapping("/api/returns")
public class ReturnController {

    private final SalesReturnService salesReturnService;

    public ReturnController(SalesReturnService salesReturnService) {
        this.salesReturnService = salesReturnService;
    }

    /**
     * Looks up an invoice for the returns workflow.
     * Returns invoice header + batch-level items with max returnable quantities.
     */
    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<?> getInvoiceForReturn(@PathVariable("invoiceId") Long invoiceId) {
        try {
            InvoiceLookupDTO result = salesReturnService.findInvoiceForReturn(invoiceId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Processes a sales return: creates SALES_RETURN + SALES_RETURN_ITEM rows,
     * restocks if applicable, and returns the refund receipt.
     */
    @PostMapping
    public ResponseEntity<?> processReturn(@RequestBody SalesReturnRequestDTO request) {
        try {
            SalesReturnResponseDTO result = salesReturnService.processReturn(request);
            return ResponseEntity.ok(result);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Returns a list of all past returns with item details and computed refund totals.
     */
    @GetMapping("/history")
    public ResponseEntity<List<SalesReturnResponseDTO>> getReturnHistory() {
        return ResponseEntity.ok(salesReturnService.getReturnHistory());
    }
}
