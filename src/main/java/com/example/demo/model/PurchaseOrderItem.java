package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "PURCHASE_ORDER_ITEM", uniqueConstraints = {
    @UniqueConstraint(name = "uq_po_medicine", columnNames = {"po_id", "medicine_id"})
})
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "po_item_id")
    private Long poItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(name = "quantity_ordered", nullable = false)
    private Integer quantityOrdered;

    @Column(name = "quantity_received")
    private Integer quantityReceived = 0;

    @Column(name = "purchase_price", precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    // --- Constructors ---

    public PurchaseOrderItem() {
    }

    public PurchaseOrderItem(PurchaseOrder purchaseOrder, Medicine medicine,
                             Integer quantityOrdered, Integer quantityReceived, BigDecimal purchasePrice) {
        this.purchaseOrder = purchaseOrder;
        this.medicine = medicine;
        this.quantityOrdered = quantityOrdered;
        this.quantityReceived = quantityReceived != null ? quantityReceived : 0;
        this.purchasePrice = purchasePrice;
    }

    // --- Getters & Setters ---

    public Long getPoItemId() {
        return poItemId;
    }

    public void setPoItemId(Long poItemId) {
        this.poItemId = poItemId;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }

    public Integer getQuantityOrdered() {
        return quantityOrdered;
    }

    public void setQuantityOrdered(Integer quantityOrdered) {
        this.quantityOrdered = quantityOrdered;
    }

    public Integer getQuantityReceived() {
        return quantityReceived != null ? quantityReceived : 0;
    }

    public void setQuantityReceived(Integer quantityReceived) {
        this.quantityReceived = quantityReceived;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }
}
