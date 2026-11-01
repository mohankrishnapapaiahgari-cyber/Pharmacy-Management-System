package com.example.demo.controller;

import com.example.demo.dto.CheckoutRequestDTO;
import com.example.demo.dto.InvoiceResponseDTO;
import com.example.demo.dto.MedicineSearchDTO;
import com.example.demo.model.Customer;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.service.InvoiceService;
import com.example.demo.service.MedicineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for the Cashier Point-of-Sale workflow.
 *
 * Endpoints:
 *   GET  /api/pos/medicines/search?keyword=...   → search available medicines
 *   GET  /api/pos/customers                       → list all customers (for dropdowns)
 *   POST /api/pos/checkout                        → execute a checkout transaction
 */
@RestController
@RequestMapping({"/api/pos", "/api"})
public class PosController {

    private final MedicineService medicineService;
    private final InvoiceService invoiceService;
    private final CustomerRepository customerRepository;

    public PosController(MedicineService medicineService, InvoiceService invoiceService,
                         CustomerRepository customerRepository) {
        this.medicineService = medicineService;
        this.invoiceService = invoiceService;
        this.customerRepository = customerRepository;
    }

    // =========================================================
    // 1. Medicine Search — used by the cashier's search bar
    // =========================================================

    /**
     * Searches medicines by name or generic name (partial, case-insensitive).
     * Only returns medicines that have non-expired, in-stock batches.
     */
    @GetMapping("/medicines/search")
    public ResponseEntity<List<MedicineSearchDTO>> searchMedicines(
            @RequestParam("keyword") String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<MedicineSearchDTO> results = medicineService.searchMedicines(keyword.trim());
        return ResponseEntity.ok(results);
    }

    /**
     * Retrieves single medicine by ID with current available stock.
     */
    @GetMapping("/medicines/{id}")
    public ResponseEntity<?> getMedicineById(@PathVariable("id") Long id) {
        try {
            MedicineSearchDTO medicine = medicineService.getMedicineById(id);
            return ResponseEntity.ok(medicine);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Retrieves all active batches for a medicine with physical shelf locations and FEFO ordering.
     */
    @GetMapping("/medicines/{id}/batches")
    public ResponseEntity<?> getMedicineBatches(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(medicineService.getMedicineBatches(id));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    // =========================================================
    // 2. Customer Management — used by POS & Pharmacist Portal
    // =========================================================

    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> listCustomers() {
        return ResponseEntity.ok(customerRepository.findAll());
    }

    @PostMapping("/customers")
    public ResponseEntity<?> createCustomer(@RequestBody Customer customer) {
        if (customer.getFullName() == null || customer.getFullName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Customer full name is required."));
        }
        if (customer.getPhone() == null || customer.getPhone().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Customer phone number is required."));
        }

        // Clean inputs
        customer.setFullName(customer.getFullName().trim());
        customer.setPhone(customer.getPhone().trim());
        if (customer.getEmail() != null) {
            customer.setEmail(customer.getEmail().trim());
        }
        if (customer.getAddress() != null) {
            customer.setAddress(customer.getAddress().trim());
        }

        Customer savedCustomer = customerRepository.save(customer);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(savedCustomer);
    }

    // =========================================================
    // 3. Checkout — atomic POS transaction with FEFO deduction
    // =========================================================

    /**
     * Processes the full checkout: creates an invoice, deducts inventory
     * using FEFO logic, and returns the computed invoice receipt.
     * If Schedule H drugs are present, a valid prescriptionId is required.
     */
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequestDTO request) {

        // Basic validation
        if (request.getItems() == null || request.getItems().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Cart is empty. Add items before checkout."));
        }

        try {
            InvoiceResponseDTO invoice = invoiceService.checkout(request);
            return ResponseEntity.ok(invoice);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", ex.getMessage()));
        }
    }
}
