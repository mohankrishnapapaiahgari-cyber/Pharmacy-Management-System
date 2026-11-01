package com.example.demo.dto;

import java.time.LocalDate;

/**
 * DTO for expiring batch monitoring.
 * Shows batch details with days remaining until expiry and shelf location.
 */
public class ExpiringBatchDTO {

    private Long batchId;
    private String batchNumber;
    private Long medicineId;
    private String medicineName;
    private int quantityInStock;
    private LocalDate expiryDate;
    private long daysUntilExpiry;
    private String shelfLocation;
    private Integer aisleNumber;
    private String rackCode;
    private Integer shelfNumber;

    public ExpiringBatchDTO() {
    }

    public ExpiringBatchDTO(Long batchId, String batchNumber, Long medicineId,
                            String medicineName, int quantityInStock,
                            LocalDate expiryDate, long daysUntilExpiry,
                            String shelfLocation, Integer aisleNumber,
                            String rackCode, Integer shelfNumber) {
        this.batchId = batchId;
        this.batchNumber = batchNumber;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.quantityInStock = quantityInStock;
        this.expiryDate = expiryDate;
        this.daysUntilExpiry = daysUntilExpiry;
        this.shelfLocation = shelfLocation;
        this.aisleNumber = aisleNumber;
        this.rackCode = rackCode;
        this.shelfNumber = shelfNumber;
    }

    public ExpiringBatchDTO(Long batchId, String batchNumber, Long medicineId,
                            String medicineName, int quantityInStock,
                            LocalDate expiryDate, long daysUntilExpiry,
                            String shelfLocation) {
        this(batchId, batchNumber, medicineId, medicineName, quantityInStock, expiryDate, daysUntilExpiry, shelfLocation, null, null, null);
    }

    public ExpiringBatchDTO(Long batchId, String batchNumber, Long medicineId,
                            String medicineName, int quantityInStock,
                            LocalDate expiryDate, long daysUntilExpiry) {
        this(batchId, batchNumber, medicineId, medicineName, quantityInStock, expiryDate, daysUntilExpiry, "Unassigned");
    }

    // --- Getters & Setters ---

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public int getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(int quantityInStock) {
        this.quantityInStock = quantityInStock;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public long getDaysUntilExpiry() {
        return daysUntilExpiry;
    }

    public void setDaysUntilExpiry(long daysUntilExpiry) {
        this.daysUntilExpiry = daysUntilExpiry;
    }

    public String getShelfLocation() {
        return shelfLocation;
    }

    public void setShelfLocation(String shelfLocation) {
        this.shelfLocation = shelfLocation;
    }

    public Integer getAisleNumber() {
        return aisleNumber;
    }

    public void setAisleNumber(Integer aisleNumber) {
        this.aisleNumber = aisleNumber;
    }

    public String getRackCode() {
        return rackCode;
    }

    public void setRackCode(String rackCode) {
        this.rackCode = rackCode;
    }

    public Integer getShelfNumber() {
        return shelfNumber;
    }

    public void setShelfNumber(Integer shelfNumber) {
        this.shelfNumber = shelfNumber;
    }
}
