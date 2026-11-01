package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Returned by the medicine search API.
 * Includes dosageForm, strength, total available stock, scheduleType,
 * physical shelf location, and active batches for granular batch-level selection.
 */
public class MedicineSearchDTO {

    private Long medicineId;
    private String medicineName;
    private String genericName;
    private String dosageForm;
    private String strength;
    private BigDecimal unitPrice;
    private int totalAvailableStock;
    private String scheduleType; // "OTC", "Schedule H", "Schedule H1", etc.
    private String primaryShelfLocation; // e.g. "Aisle 1, Rack A1, Shelf 2"
    private List<MedicineBatchDTO> batches = new ArrayList<>();

    public MedicineSearchDTO() {
    }

    public MedicineSearchDTO(Long medicineId, String medicineName, String genericName,
                             String dosageForm, String strength,
                             BigDecimal unitPrice, int totalAvailableStock, String scheduleType) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.genericName = genericName;
        this.dosageForm = dosageForm;
        this.strength = strength;
        this.unitPrice = unitPrice;
        this.totalAvailableStock = totalAvailableStock;
        this.scheduleType = scheduleType;
    }

    public MedicineSearchDTO(Long medicineId, String medicineName, String genericName,
                             BigDecimal unitPrice, int totalAvailableStock, String scheduleType) {
        this(medicineId, medicineName, genericName, "Tablet", "", unitPrice, totalAvailableStock, scheduleType);
    }

    // --- Getters & Setters ---

    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }

    public String getDosageForm() { return dosageForm; }
    public void setDosageForm(String dosageForm) { this.dosageForm = dosageForm; }

    public String getStrength() { return strength; }
    public void setStrength(String strength) { this.strength = strength; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public int getTotalAvailableStock() { return totalAvailableStock; }
    public void setTotalAvailableStock(int totalAvailableStock) { this.totalAvailableStock = totalAvailableStock; }

    public String getScheduleType() { return scheduleType; }
    public void setScheduleType(String scheduleType) { this.scheduleType = scheduleType; }

    public String getPrimaryShelfLocation() { return primaryShelfLocation; }
    public void setPrimaryShelfLocation(String primaryShelfLocation) { this.primaryShelfLocation = primaryShelfLocation; }

    public List<MedicineBatchDTO> getBatches() { return batches; }
    public void setBatches(List<MedicineBatchDTO> batches) { this.batches = batches != null ? batches : new ArrayList<>(); }
}
