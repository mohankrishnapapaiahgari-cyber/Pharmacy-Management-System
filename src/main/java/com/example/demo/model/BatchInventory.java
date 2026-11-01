package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "BATCH_INVENTORY")
public class BatchInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_id")
    private Long batchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(name = "batch_number", nullable = false)
    private String batchNumber;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "mfg_date")
    private LocalDate mfgDate;

    @Column(name = "quantity_in_stock", nullable = false)
    private Integer quantityInStock;

    @Column(name = "purchase_unit_cost", precision = 10, scale = 2)
    private BigDecimal purchaseUnitCost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = true)
    private ShelfLocation location;

    // --- Constructors ---

    public BatchInventory() {
    }

    public BatchInventory(Medicine medicine, String batchNumber,
                          LocalDate expiryDate, Integer quantityInStock) {
        this.medicine = medicine;
        this.batchNumber = batchNumber;
        this.expiryDate = expiryDate;
        this.quantityInStock = quantityInStock;
    }

    // --- Getters & Setters ---

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
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

    public Integer getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(Integer quantityInStock) {
        this.quantityInStock = quantityInStock;
    }

    public BigDecimal getPurchaseUnitCost() {
        return purchaseUnitCost;
    }

    public void setPurchaseUnitCost(BigDecimal purchaseUnitCost) {
        this.purchaseUnitCost = purchaseUnitCost;
    }

    public ShelfLocation getLocation() {
        return location;
    }

    public void setLocation(ShelfLocation location) {
        this.location = location;
    }
}
