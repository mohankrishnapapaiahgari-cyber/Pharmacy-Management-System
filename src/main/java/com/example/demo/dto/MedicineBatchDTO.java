package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Detailed DTO for a medicine batch with physical shelf location and FEFO recommendation status.
 */
public class MedicineBatchDTO {

    private Long batchId;
    private Long medicineId;
    private String medicineName;
    private String batchNumber;
    private LocalDate expiryDate;
    private LocalDate mfgDate;
    private int quantityInStock;
    private BigDecimal purchaseUnitCost;
    private BigDecimal unitPrice;
    private Long locationId;
    private Integer aisleNumber;
    private String rackCode;
    private Integer shelfNumber;
    private String temperatureZone;
    private String shelfLocationDisplay;
    private boolean recommendedFefo;

    public MedicineBatchDTO() {
    }

    public MedicineBatchDTO(Long batchId, Long medicineId, String medicineName, String batchNumber,
                            LocalDate expiryDate, LocalDate mfgDate, int quantityInStock,
                            BigDecimal purchaseUnitCost, BigDecimal unitPrice, Long locationId,
                            Integer aisleNumber, String rackCode, Integer shelfNumber,
                            String temperatureZone, String shelfLocationDisplay, boolean recommendedFefo) {
        this.batchId = batchId;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.batchNumber = batchNumber;
        this.expiryDate = expiryDate;
        this.mfgDate = mfgDate;
        this.quantityInStock = quantityInStock;
        this.purchaseUnitCost = purchaseUnitCost;
        this.unitPrice = unitPrice;
        this.locationId = locationId;
        this.aisleNumber = aisleNumber;
        this.rackCode = rackCode;
        this.shelfNumber = shelfNumber;
        this.temperatureZone = temperatureZone;
        this.shelfLocationDisplay = shelfLocationDisplay;
        this.recommendedFefo = recommendedFefo;
    }

    // --- Getters & Setters ---

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
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

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public LocalDate getMfgDate() {
        return mfgDate;
    }

    public void setMfgDate(LocalDate mfgDate) {
        this.mfgDate = mfgDate;
    }

    public int getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(int quantityInStock) {
        this.quantityInStock = quantityInStock;
    }

    public BigDecimal getPurchaseUnitCost() {
        return purchaseUnitCost;
    }

    public void setPurchaseUnitCost(BigDecimal purchaseUnitCost) {
        this.purchaseUnitCost = purchaseUnitCost;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
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

    public String getTemperatureZone() {
        return temperatureZone;
    }

    public void setTemperatureZone(String temperatureZone) {
        this.temperatureZone = temperatureZone;
    }

    public String getShelfLocationDisplay() {
        return shelfLocationDisplay;
    }

    public void setShelfLocationDisplay(String shelfLocationDisplay) {
        this.shelfLocationDisplay = shelfLocationDisplay;
    }

    public boolean isRecommendedFefo() {
        return recommendedFefo;
    }

    public void setRecommendedFefo(boolean recommendedFefo) {
        this.recommendedFefo = recommendedFefo;
    }
}
