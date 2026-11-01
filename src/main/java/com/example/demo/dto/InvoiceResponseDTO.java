package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response returned after a successful checkout.
 * All financial totals are computed dynamically — nothing stored in DB (strict 3NF).
 */
public class InvoiceResponseDTO {

    private Long invoiceId;
    private LocalDateTime invoiceDate;
    private String paymentStatus;
    private String paymentMethod;
    private String transactionReference;
    private List<InvoiceItemDetail> items;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal totalTax;
    private BigDecimal grandTotal;

    // --- Getters & Setters ---

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public LocalDateTime getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDateTime invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public List<InvoiceItemDetail> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItemDetail> items) {
        this.items = items;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount != null ? discountAmount : BigDecimal.ZERO;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(BigDecimal totalTax) {
        this.totalTax = totalTax;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal;
    }

    // =====================================================
    // Nested DTO for each line item in the invoice response
    // =====================================================
    public static class InvoiceItemDetail {

        private Long invoiceItemId;
        private Long batchId;
        private String medicineName;
        private String batchNumber;
        private int quantity;
        private BigDecimal unitSellingPrice;
        private BigDecimal taxRate;
        private BigDecimal lineTotal;  // computed: (unitPrice * qty) + tax

        // --- Getters & Setters ---

        public Long getInvoiceItemId() {
            return invoiceItemId;
        }

        public void setInvoiceItemId(Long invoiceItemId) {
            this.invoiceItemId = invoiceItemId;
        }

        public Long getBatchId() {
            return batchId;
        }

        public void setBatchId(Long batchId) {
            this.batchId = batchId;
        }

        public String getMedicineName() {
            return medicineName;
        }

        public void setMedicineName(String medicineName) {
            this.medicineName = medicineName;
        }

        public String getBatchNumber() {
            return batchNumber;
        }

        public void setBatchNumber(String batchNumber) {
            this.batchNumber = batchNumber;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitSellingPrice() {
            return unitSellingPrice;
        }

        public void setUnitSellingPrice(BigDecimal unitSellingPrice) {
            this.unitSellingPrice = unitSellingPrice;
        }

        public BigDecimal getTaxRate() {
            return taxRate;
        }

        public void setTaxRate(BigDecimal taxRate) {
            this.taxRate = taxRate;
        }

        public BigDecimal getLineTotal() {
            return lineTotal;
        }

        public void setLineTotal(BigDecimal lineTotal) {
            this.lineTotal = lineTotal;
        }
    }
}
