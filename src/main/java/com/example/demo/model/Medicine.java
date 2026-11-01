package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "MEDICINE")
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicine_id")
    private Long medicineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturer_id")
    private Manufacturer manufacturer;

    @Column(name = "medicine_name", nullable = false)
    private String medicineName;

    @Column(name = "generic_name")
    private String genericName;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "dosage_form", nullable = false)
    private String dosageForm = "Tablet";

    @Column(name = "strength")
    private String strength;

    @Column(name = "schedule_type")
    private String scheduleType;  // e.g., "OTC", "Schedule H", "Schedule H1"

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "reorder_level", nullable = false)
    private Integer reorderLevel = 10;

    @Column(name = "reorder_quantity", nullable = false)
    private Integer reorderQuantity = 50;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // --- Constructors ---

    public Medicine() {
    }

    public Medicine(String medicineName, String genericName, BigDecimal unitPrice) {
        this.medicineName = medicineName;
        this.genericName = genericName;
        this.unitPrice = unitPrice;
        this.dosageForm = "Tablet";
        this.reorderLevel = 10;
        this.reorderQuantity = 50;
        this.isActive = true;
    }

    public Medicine(String medicineName, String genericName, String brandName, String dosageForm,
                    String strength, String scheduleType, BigDecimal unitPrice,
                    Integer reorderLevel, Integer reorderQuantity, Boolean isActive) {
        this.medicineName = medicineName;
        this.genericName = genericName;
        this.brandName = brandName;
        this.dosageForm = dosageForm;
        this.strength = strength;
        this.scheduleType = scheduleType;
        this.unitPrice = unitPrice;
        this.reorderLevel = reorderLevel != null ? reorderLevel : 10;
        this.reorderQuantity = reorderQuantity != null ? reorderQuantity : 50;
        this.isActive = isActive != null ? isActive : true;
    }

    // --- Getters & Setters ---

    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Manufacturer getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(Manufacturer manufacturer) {
        this.manufacturer = manufacturer;
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

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getDosageForm() {
        return dosageForm;
    }

    public void setDosageForm(String dosageForm) {
        this.dosageForm = dosageForm;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(Integer reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public Integer getReorderQuantity() {
        return reorderQuantity;
    }

    public void setReorderQuantity(Integer reorderQuantity) {
        this.reorderQuantity = reorderQuantity;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
