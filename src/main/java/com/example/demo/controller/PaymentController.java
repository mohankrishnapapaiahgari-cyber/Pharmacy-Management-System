package com.example.demo.controller;

import com.example.demo.exception.PharmacyBusinessException;
import com.example.demo.model.Invoice;
import com.example.demo.model.InvoiceItem;
import com.example.demo.model.Payment;
import com.example.demo.repository.InvoiceItemRepository;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.PaymentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final PaymentRepository paymentRepository;

    public PaymentController(InvoiceRepository invoiceRepository,
                             InvoiceItemRepository invoiceItemRepository,
                             PaymentRepository paymentRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.paymentRepository = paymentRepository;
    }

    /**
     * Lists invoices with computed grand totals, amount paid, and balance due.
     */
    @GetMapping("/invoices")
    public ResponseEntity<List<Map<String, Object>>> listInvoices(
            @RequestParam(name = "status", required = false) String status) {
        List<Invoice> invoices = invoiceRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Invoice inv : invoices) {
            if (status != null && !status.isBlank() && !inv.getPaymentStatus().equalsIgnoreCase(status)) {
                continue;
            }

            List<InvoiceItem> items = invoiceItemRepository.findByInvoiceIdWithDetails(inv.getInvoiceId());
            BigDecimal subtotal = BigDecimal.ZERO;
            BigDecimal tax = BigDecimal.ZERO;

            for (InvoiceItem ii : items) {
                BigDecimal lineSub = ii.getUnitSellingPrice().multiply(BigDecimal.valueOf(ii.getQuantity()));
                BigDecimal lineTax = lineSub.multiply(ii.getTaxRate()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                subtotal = subtotal.add(lineSub);
                tax = tax.add(lineTax);
            }

            BigDecimal discount = inv.getDiscountAmount() != null ? inv.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal grandTotal = subtotal.subtract(discount).add(tax);
            if (grandTotal.compareTo(BigDecimal.ZERO) < 0) grandTotal = BigDecimal.ZERO;

            List<Payment> payments = paymentRepository.findByInvoiceInvoiceId(inv.getInvoiceId());
            BigDecimal totalPaid = payments.stream()
                    .map(Payment::getAmountPaid)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal balanceDue = grandTotal.subtract(totalPaid);
            if (balanceDue.compareTo(BigDecimal.ZERO) < 0) balanceDue = BigDecimal.ZERO;

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("invoiceId", inv.getInvoiceId());
            map.put("invoiceDate", inv.getInvoiceDate());
            map.put("customerName", inv.getCustomer() != null ? inv.getCustomer().getFullName() : "Walk-in Customer");
            map.put("cashierName", inv.getEmployee() != null ? inv.getEmployee().getFirstName() + " " + inv.getEmployee().getLastName() : "Staff");
            map.put("paymentStatus", inv.getPaymentStatus());
            map.put("subtotal", subtotal);
            map.put("discountAmount", discount);
            map.put("totalTax", tax);
            map.put("grandTotal", grandTotal);
            map.put("totalPaid", totalPaid);
            map.put("balanceDue", balanceDue);
            result.add(map);
        }

        // Sort descending by invoiceId
        result.sort((a, b) -> Long.compare((Long) b.get("invoiceId"), (Long) a.get("invoiceId")));
        return ResponseEntity.ok(result);
    }

    /**
     * Fetches details of a specific invoice including items and payments.
     */
    @GetMapping("/invoices/{id}")
    public ResponseEntity<?> getInvoiceDetail(@PathVariable("id") Long id) {
        Invoice inv = invoiceRepository.findById(id)
                .orElseThrow(() -> new PharmacyBusinessException("E48", "Invoice #" + id + " not found."));

        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceIdWithDetails(id);
        List<Payment> payments = paymentRepository.findByInvoiceInvoiceId(id);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        List<Map<String, Object>> itemDetails = new ArrayList<>();

        for (InvoiceItem ii : items) {
            BigDecimal lineSub = ii.getUnitSellingPrice().multiply(BigDecimal.valueOf(ii.getQuantity()));
            BigDecimal lineTax = lineSub.multiply(ii.getTaxRate()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(lineSub);
            tax = tax.add(lineTax);

            Map<String, Object> it = new LinkedHashMap<>();
            it.put("medicineName", ii.getBatch().getMedicine().getMedicineName());
            it.put("batchNumber", ii.getBatch().getBatchNumber());
            it.put("quantity", ii.getQuantity());
            it.put("unitSellingPrice", ii.getUnitSellingPrice());
            it.put("lineTotal", lineSub.add(lineTax));
            itemDetails.add(it);
        }

        BigDecimal discount = inv.getDiscountAmount() != null ? inv.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal grandTotal = subtotal.subtract(discount).add(tax);
        BigDecimal totalPaid = payments.stream().map(Payment::getAmountPaid).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal balance = grandTotal.subtract(totalPaid);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("invoiceId", inv.getInvoiceId());
        resp.put("invoiceDate", inv.getInvoiceDate());
        resp.put("customerName", inv.getCustomer() != null ? inv.getCustomer().getFullName() : "Walk-in Customer");
        resp.put("paymentStatus", inv.getPaymentStatus());
        resp.put("subtotal", subtotal);
        resp.put("discountAmount", discount);
        resp.put("totalTax", tax);
        resp.put("grandTotal", grandTotal);
        resp.put("totalPaid", totalPaid);
        resp.put("balanceDue", balance.max(BigDecimal.ZERO));
        resp.put("items", itemDetails);
        resp.put("payments", payments);

        return ResponseEntity.ok(resp);
    }

    /**
     * Records a payment against an invoice (E47 - E51).
     */
    @PostMapping("/record")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> recordPayment(@RequestBody Map<String, Object> payload) {
        Long invoiceId = Long.parseLong(payload.get("invoiceId").toString());
        BigDecimal amount = new BigDecimal(payload.get("amountPaid").toString());
        String method = payload.get("paymentMethod") != null ? payload.get("paymentMethod").toString().toUpperCase() : "CASH";
        String txnRef = (String) payload.get("transactionReference");

        // E47: amount_paid <= 0
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PharmacyBusinessException("E47", "Payment amount must be greater than zero.");
        }

        // E51: Invalid payment method
        List<String> validMethods = List.of("CASH", "CARD", "UPI", "NET_BANKING");
        if (!validMethods.contains(method)) {
            throw new PharmacyBusinessException("E51", "Invalid payment method: " + method + ". Allowed: CASH, CARD, UPI, NET_BANKING.");
        }

        // E48: Invalid invoice
        Invoice inv = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new PharmacyBusinessException("E48", "Invoice #" + invoiceId + " does not exist."));

        // E49: Already fully paid
        if ("PAID".equalsIgnoreCase(inv.getPaymentStatus())) {
            throw new PharmacyBusinessException("E49", "Invoice #" + invoiceId + " is already fully paid.");
        }

        // Calculate current balance due
        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceIdWithDetails(invoiceId);
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        for (InvoiceItem ii : items) {
            BigDecimal lineSub = ii.getUnitSellingPrice().multiply(BigDecimal.valueOf(ii.getQuantity()));
            BigDecimal lineTax = lineSub.multiply(ii.getTaxRate()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(lineSub);
            tax = tax.add(lineTax);
        }
        BigDecimal discount = inv.getDiscountAmount() != null ? inv.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal grandTotal = subtotal.subtract(discount).add(tax);

        List<Payment> pastPayments = paymentRepository.findByInvoiceInvoiceId(invoiceId);
        BigDecimal alreadyPaid = pastPayments.stream().map(Payment::getAmountPaid).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal balance = grandTotal.subtract(alreadyPaid);

        // E50: Overpayment check
        if (amount.compareTo(balance) > 0) {
            throw new PharmacyBusinessException("E50", "Payment of ₹" + amount + " exceeds remaining balance of ₹" + balance);
        }

        Payment payment = new Payment();
        payment.setInvoice(inv);
        payment.setAmountPaid(amount);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentMethod(method);
        payment.setTransactionReference(txnRef);
        paymentRepository.save(payment);

        BigDecimal newTotalPaid = alreadyPaid.add(amount);
        if (newTotalPaid.compareTo(grandTotal) >= 0) {
            inv.setPaymentStatus("PAID");
        } else {
            inv.setPaymentStatus("PARTIALLY_PAID");
        }
        invoiceRepository.save(inv);

        return ResponseEntity.ok(Map.of(
                "message", "Payment of ₹" + amount + " recorded successfully.",
                "paymentId", payment.getPaymentId(),
                "paymentStatus", inv.getPaymentStatus(),
                "remainingBalance", grandTotal.subtract(newTotalPaid).max(BigDecimal.ZERO)
        ));
    }
}
