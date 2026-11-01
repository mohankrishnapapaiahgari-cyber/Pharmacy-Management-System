package com.example.demo.dto;

/**
 * DTO for low-stock alert reporting.
 * Aggregates non-expired stock per medicine and compares against reorder level.
 */
public class LowStockAlertDTO {

    private Long medicineId;
    private String medicineName;
    private String genericName;
    private String categoryName;
    private int currentStock;
    private int reorderLevel;
    private int reorderQuantity;

    public LowStockAlertDTO() {
    }

    public LowStockAlertDTO(Long medicineId, String medicineName, String genericName,
                            String categoryName, int currentStock, int reorderLevel,
                            int reorderQuantity) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.genericName = genericName;
        this.categoryName = categoryName;
        this.currentStock = currentStock;
        this.reorderLevel = reorderLevel;
        this.reorderQuantity = reorderQuantity;
    }

    public LowStockAlertDTO(Long medicineId, String medicineName, String genericName,
                            int totalStock, int reorderLevel) {
        this(medicineId, medicineName, genericName, "General", totalStock, reorderLevel, 50);
    }

    // --- Getters & Setters ---

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

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(int currentStock) {
        this.currentStock = currentStock;
    }

    // Backward compatibility with previous totalStock property
    public int getTotalStock() {
        return currentStock;
    }

    public void setTotalStock(int totalStock) {
        this.currentStock = totalStock;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public int getReorderQuantity() {
        return reorderQuantity;
    }

    public void setReorderQuantity(int reorderQuantity) {
        this.reorderQuantity = reorderQuantity;
    }
}
