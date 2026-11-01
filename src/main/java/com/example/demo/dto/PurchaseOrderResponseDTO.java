package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PurchaseOrderResponseDTO {

    private Long poId;
    private Long supplierId;
    private String supplierName;
    private Long createdByEmployeeId;
    private String createdByEmployeeName;
    private LocalDate orderDate;
    private String orderStatus;
    private LocalDate expectedDeliveryDate;
    private List<ItemDTO> items;

    public PurchaseOrderResponseDTO() {
    }

    public PurchaseOrderResponseDTO(Long poId, Long supplierId, String supplierName,
                                    Long createdByEmployeeId, String createdByEmployeeName,
                                    LocalDate orderDate, String orderStatus,
                                    LocalDate expectedDeliveryDate, List<ItemDTO> items) {
        this.poId = poId;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.createdByEmployeeId = createdByEmployeeId;
        this.createdByEmployeeName = createdByEmployeeName;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.items = items;
    }

    public static class ItemDTO {
        private Long medicineId;
        private String medicineName;
        private Integer quantityOrdered;
        private Integer quantityReceived;
        private BigDecimal purchasePrice;

        public ItemDTO() {}

        public ItemDTO(Long medicineId, String medicineName, Integer quantityOrdered,
                       Integer quantityReceived, BigDecimal purchasePrice) {
            this.medicineId = medicineId;
            this.medicineName = medicineName;
            this.quantityOrdered = quantityOrdered;
            this.quantityReceived = quantityReceived;
            this.purchasePrice = purchasePrice;
        }

        public Long getMedicineId() { return medicineId; }
        public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }
        public String getMedicineName() { return medicineName; }
        public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
        public Integer getQuantityOrdered() { return quantityOrdered; }
        public void setQuantityOrdered(Integer quantityOrdered) { this.quantityOrdered = quantityOrdered; }
        public Integer getQuantityReceived() { return quantityReceived; }
        public void setQuantityReceived(Integer quantityReceived) { this.quantityReceived = quantityReceived; }
        public BigDecimal getPurchasePrice() { return purchasePrice; }
        public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
    }

    // Alias for backward compatibility
    public static class POItemDetailDTO extends ItemDTO {
        public POItemDetailDTO(Long medicineId, String medicineName, Integer quantityOrdered,
                               Integer quantityReceived, BigDecimal purchasePrice) {
            super(medicineId, medicineName, quantityOrdered, quantityReceived, purchasePrice);
        }
    }

    // --- Getters & Setters ---

    public Long getPoId() { return poId; }
    public void setPoId(Long poId) { this.poId = poId; }

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public Long getCreatedByEmployeeId() { return createdByEmployeeId; }
    public void setCreatedByEmployeeId(Long createdByEmployeeId) { this.createdByEmployeeId = createdByEmployeeId; }

    public String getCreatedByEmployeeName() { return createdByEmployeeName; }
    public void setCreatedByEmployeeName(String createdByEmployeeName) { this.createdByEmployeeName = createdByEmployeeName; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }

    public List<ItemDTO> getItems() { return items; }
    public void setItems(List<ItemDTO> items) { this.items = items; }
}
