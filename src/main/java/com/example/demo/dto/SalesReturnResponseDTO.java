package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for a processed sales return.
 * Includes the return details, item-level breakdown, and computed refund total.
 */
public class SalesReturnResponseDTO {

    private Long returnId;
    private Long invoiceId;
    private String processedByName;
    private LocalDateTime returnDate;
    private String reason;
    private List<ReturnItemDetail> items;
    private BigDecimal totalRefund;

    // --- Getters & Setters ---

    public Long getReturnId() { return returnId; }
    public void setReturnId(Long returnId) { this.returnId = returnId; }

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }

    public String getProcessedByName() { return processedByName; }
    public void setProcessedByName(String processedByName) { this.processedByName = processedByName; }

    public LocalDateTime getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDateTime returnDate) { this.returnDate = returnDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public List<ReturnItemDetail> getItems() { return items; }
    public void setItems(List<ReturnItemDetail> items) { this.items = items; }

    public BigDecimal getTotalRefund() { return totalRefund; }
    public void setTotalRefund(BigDecimal totalRefund) { this.totalRefund = totalRefund; }

    /**
     * Individual returned item detail.
     */
    public static class ReturnItemDetail {

        private Long batchId;
        private String batchNumber;
        private String medicineName;
        private Integer quantityReturned;
        private BigDecimal refundPrice;
        private BigDecimal lineRefund;  // computed: refundPrice * quantityReturned
        private String disposition;

        // --- Getters & Setters ---

        public Long getBatchId() { return batchId; }
        public void setBatchId(Long batchId) { this.batchId = batchId; }

        public String getBatchNumber() { return batchNumber; }
        public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

        public String getMedicineName() { return medicineName; }
        public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

        public Integer getQuantityReturned() { return quantityReturned; }
        public void setQuantityReturned(Integer quantityReturned) { this.quantityReturned = quantityReturned; }

        public BigDecimal getRefundPrice() { return refundPrice; }
        public void setRefundPrice(BigDecimal refundPrice) { this.refundPrice = refundPrice; }

        public BigDecimal getLineRefund() { return lineRefund; }
        public void setLineRefund(BigDecimal lineRefund) { this.lineRefund = lineRefund; }

        public String getDisposition() { return disposition; }
        public void setDisposition(String disposition) { this.disposition = disposition; }
    }
}
