package com.example.demo.dto;

import java.util.List;

/**
 * Request payload for processing a sales return / refund.
 */
public class SalesReturnRequestDTO {

    private Long invoiceId;
    private Long employeeId;       // who is processing the return
    private String reason;
    private List<ReturnItemDTO> items;

    public SalesReturnRequestDTO() {
    }

    // --- Getters & Setters ---

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public List<ReturnItemDTO> getItems() { return items; }
    public void setItems(List<ReturnItemDTO> items) { this.items = items; }

    /**
     * Each item being returned — identified by invoice_item_id or batchId with quantity and disposition.
     */
    public static class ReturnItemDTO {

        private Long invoiceItemId;
        private Long batchId;
        private Integer quantityReturned;
        private String disposition;  // 'RETURNED_TO_STOCK' (or 'RESTOCK') vs 'DISCARDED' (or 'DAMAGED_DISPOSAL')

        public ReturnItemDTO() {
        }

        public Long getInvoiceItemId() { return invoiceItemId; }
        public void setInvoiceItemId(Long invoiceItemId) { this.invoiceItemId = invoiceItemId; }

        public Long getBatchId() { return batchId; }
        public void setBatchId(Long batchId) { this.batchId = batchId; }

        public Integer getQuantityReturned() { return quantityReturned; }
        public void setQuantityReturned(Integer quantityReturned) { this.quantityReturned = quantityReturned; }

        public String getDisposition() { return disposition; }
        public void setDisposition(String disposition) { this.disposition = disposition; }
    }
}
