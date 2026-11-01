package com.example.demo.dto;

/**
 * Represents a single item in the checkout request payload.
 * The cashier sends medicineId + quantity; optionally provides batchId
 * to deduct from a specific physical batch.
 */
public class CheckoutItemDTO {

    private Long medicineId;
    private Long batchId; // nullable: specific batch override
    private int quantity;

    public CheckoutItemDTO() {
    }

    public CheckoutItemDTO(Long medicineId, int quantity) {
        this.medicineId = medicineId;
        this.quantity = quantity;
    }

    public CheckoutItemDTO(Long medicineId, Long batchId, int quantity) {
        this.medicineId = medicineId;
        this.batchId = batchId;
        this.quantity = quantity;
    }

    // --- Getters & Setters ---

    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
