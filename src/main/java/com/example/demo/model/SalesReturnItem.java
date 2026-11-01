package com.example.demo.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "SALES_RETURN_ITEM")
@IdClass(SalesReturnItem.SalesReturnItemId.class)
public class SalesReturnItem {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    private SalesReturn salesReturn;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_item_id", nullable = false)
    private InvoiceItem invoiceItem;

    @Column(name = "quantity_returned", nullable = false)
    private Integer quantityReturned;

    @Column(name = "refund_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal refundPrice;

    @Column(name = "disposition", nullable = false)
    private String disposition; // 'RETURNED_TO_STOCK', 'DISCARDED'

    // --- Constructors ---

    public SalesReturnItem() {
    }

    public SalesReturnItem(SalesReturn salesReturn, InvoiceItem invoiceItem,
                           Integer quantityReturned, BigDecimal refundPrice, String disposition) {
        this.salesReturn = salesReturn;
        this.invoiceItem = invoiceItem;
        this.quantityReturned = quantityReturned;
        this.refundPrice = refundPrice;
        this.disposition = disposition;
    }

    // --- Getters & Setters ---

    public SalesReturn getSalesReturn() {
        return salesReturn;
    }

    public void setSalesReturn(SalesReturn salesReturn) {
        this.salesReturn = salesReturn;
    }

    public InvoiceItem getInvoiceItem() {
        return invoiceItem;
    }

    public void setInvoiceItem(InvoiceItem invoiceItem) {
        this.invoiceItem = invoiceItem;
    }

    public Integer getQuantityReturned() {
        return quantityReturned;
    }

    public void setQuantityReturned(Integer quantityReturned) {
        this.quantityReturned = quantityReturned;
    }

    public BigDecimal getRefundPrice() {
        return refundPrice;
    }

    public void setRefundPrice(BigDecimal refundPrice) {
        this.refundPrice = refundPrice;
    }

    public String getDisposition() {
        return disposition;
    }

    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }

    // Helper to get batch through invoice item
    public BatchInventory getBatch() {
        return invoiceItem != null ? invoiceItem.getBatch() : null;
    }

    // ===================================================
    // Composite Primary Key class for (return_id, invoice_item_id)
    // ===================================================
    public static class SalesReturnItemId implements Serializable {

        private Long salesReturn;
        private Long invoiceItem;

        public SalesReturnItemId() {
        }

        public SalesReturnItemId(Long salesReturn, Long invoiceItem) {
            this.salesReturn = salesReturn;
            this.invoiceItem = invoiceItem;
        }

        public Long getSalesReturn() {
            return salesReturn;
        }

        public void setSalesReturn(Long salesReturn) {
            this.salesReturn = salesReturn;
        }

        public Long getInvoiceItem() {
            return invoiceItem;
        }

        public void setInvoiceItem(Long invoiceItem) {
            this.invoiceItem = invoiceItem;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SalesReturnItemId that = (SalesReturnItemId) o;
            return Objects.equals(salesReturn, that.salesReturn) &&
                   Objects.equals(invoiceItem, that.invoiceItem);
        }

        @Override
        public int hashCode() {
            return Objects.hash(salesReturn, invoiceItem);
        }
    }
}
