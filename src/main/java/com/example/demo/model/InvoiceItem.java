package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "INVOICE_ITEM", uniqueConstraints = {
    @UniqueConstraint(name = "uq_invoice_batch", columnNames = {"invoice_id", "batch_id"})
})
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_item_id")
    private Long invoiceItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private BatchInventory batch;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_selling_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitSellingPrice;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate;

    // --- Constructors ---

    public InvoiceItem() {
    }

    public InvoiceItem(Invoice invoice, BatchInventory batch,
                       Integer quantity, BigDecimal unitSellingPrice, BigDecimal taxRate) {
        this.invoice = invoice;
        this.batch = batch;
        this.quantity = quantity;
        this.unitSellingPrice = unitSellingPrice;
        this.taxRate = taxRate;
    }

    public InvoiceItem(Long invoiceItemId, Invoice invoice, BatchInventory batch,
                       Integer quantity, BigDecimal unitSellingPrice, BigDecimal taxRate) {
        this.invoiceItemId = invoiceItemId;
        this.invoice = invoice;
        this.batch = batch;
        this.quantity = quantity;
        this.unitSellingPrice = unitSellingPrice;
        this.taxRate = taxRate;
    }

    // --- Getters & Setters ---

    public Long getInvoiceItemId() {
        return invoiceItemId;
    }

    public void setInvoiceItemId(Long invoiceItemId) {
        this.invoiceItemId = invoiceItemId;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public BatchInventory getBatch() {
        return batch;
    }

    public void setBatch(BatchInventory batch) {
        this.batch = batch;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitSellingPrice() {
        return unitSellingPrice;
    }

    public void setUnitSellingPrice(BigDecimal unitSellingPrice) {
        this.unitSellingPrice = unitSellingPrice;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }
}
