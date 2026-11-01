package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class StockIntakeRequestDTO {

    private Long poId;
    private Long medicineId;
    private Integer quantityReceived;
    private String batchNumber;
    private LocalDate expiryDate;
    private LocalDate mfgDate;
    private BigDecimal purchaseUnitCost;
    private Long locationId; // optional shelf location

    // --- Getters & Setters ---

    public Long getPoId() { return poId; }
    public void setPoId(Long poId) { this.poId = poId; }

    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

    public Integer getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(Integer quantityReceived) { this.quantityReceived = quantityReceived; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public LocalDate getMfgDate() { return mfgDate; }
    public void setMfgDate(LocalDate mfgDate) { this.mfgDate = mfgDate; }

    public BigDecimal getPurchaseUnitCost() { return purchaseUnitCost; }
    public void setPurchaseUnitCost(BigDecimal purchaseUnitCost) { this.purchaseUnitCost = purchaseUnitCost; }

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
}
