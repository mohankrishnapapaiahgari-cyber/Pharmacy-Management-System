package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO used when looking up an invoice for the Returns workflow.
 * Shows the invoice header info plus each batch-level line item
 * with remaining returnable quantities.
 */
public class InvoiceLookupDTO {

    private Long invoiceId;
    private LocalDateTime invoiceDate;
    private String customerName;
    private String paymentStatus;
    private String paymentMethod;
    private List<InvoiceBatchItem> items;
    private BigDecimal grandTotal;

    // --- Getters & Setters ---

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }

    public LocalDateTime getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDateTime invoiceDate) { this.invoiceDate = invoiceDate; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public List<InvoiceBatchItem> getItems() { return items; }
    public void setItems(List<InvoiceBatchItem> items) { this.items = items; }

    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }

    /**
     * Each line item from the invoice, with max returnable qty.
     */
    public static class InvoiceBatchItem {

        private Long invoiceItemId;
        private Long batchId;
        private String batchNumber;
        private Long medicineId;
        private String medicineName;
        private Integer quantitySold;
        private Integer quantityAlreadyReturned;
        private Integer maxReturnable;
        private BigDecimal unitSellingPrice;
        private BigDecimal taxRate;

        // --- Getters & Setters ---

        public Long getInvoiceItemId() { return invoiceItemId; }
        public void setInvoiceItemId(Long invoiceItemId) { this.invoiceItemId = invoiceItemId; }

        public Long getBatchId() { return batchId; }
        public void setBatchId(Long batchId) { this.batchId = batchId; }

        public String getBatchNumber() { return batchNumber; }
        public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

        public Long getMedicineId() { return medicineId; }
        public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

        public String getMedicineName() { return medicineName; }
        public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

        public Integer getQuantitySold() { return quantitySold; }
        public void setQuantitySold(Integer quantitySold) { this.quantitySold = quantitySold; }

        public Integer getQuantityAlreadyReturned() { return quantityAlreadyReturned; }
        public void setQuantityAlreadyReturned(Integer quantityAlreadyReturned) { this.quantityAlreadyReturned = quantityAlreadyReturned; }

        public Integer getMaxReturnable() { return maxReturnable; }
        public void setMaxReturnable(Integer maxReturnable) { this.maxReturnable = maxReturnable; }

        public BigDecimal getUnitSellingPrice() { return unitSellingPrice; }
        public void setUnitSellingPrice(BigDecimal unitSellingPrice) { this.unitSellingPrice = unitSellingPrice; }

        public BigDecimal getTaxRate() { return taxRate; }
        public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }
    }
}
