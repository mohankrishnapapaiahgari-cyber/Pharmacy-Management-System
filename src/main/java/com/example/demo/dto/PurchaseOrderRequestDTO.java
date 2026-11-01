package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PurchaseOrderRequestDTO {

    private Long supplierId;
    private Long createdByEmployeeId;
    private LocalDate expectedDeliveryDate;
    private List<PurchaseOrderItemDTO> items;

    public static class PurchaseOrderItemDTO {
        private Long medicineId;
        private Integer quantityOrdered;
        private BigDecimal purchasePrice;

        public Long getMedicineId() { return medicineId; }
        public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

        public Integer getQuantityOrdered() { return quantityOrdered; }
        public void setQuantityOrdered(Integer quantityOrdered) { this.quantityOrdered = quantityOrdered; }

        public BigDecimal getPurchasePrice() { return purchasePrice; }
        public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
    }

    // --- Getters & Setters ---

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }

    public Long getCreatedByEmployeeId() { return createdByEmployeeId; }
    public void setCreatedByEmployeeId(Long createdByEmployeeId) { this.createdByEmployeeId = createdByEmployeeId; }

    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }

    public List<PurchaseOrderItemDTO> getItems() { return items; }
    public void setItems(List<PurchaseOrderItemDTO> items) { this.items = items; }
}
