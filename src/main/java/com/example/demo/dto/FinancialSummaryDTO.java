package com.example.demo.dto;

import java.math.BigDecimal;

/**
 * DTO for financial summary and executive KPI reporting.
 * All values are dynamically computed from InvoiceItem records.
 */
public class FinancialSummaryDTO {

    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private BigDecimal totalTaxCollected = BigDecimal.ZERO;
    private BigDecimal grandTotal = BigDecimal.ZERO;
    private long transactionCount = 0L;
    private long totalItemsSold = 0L;

    public FinancialSummaryDTO() {
    }

    public FinancialSummaryDTO(BigDecimal totalRevenue, BigDecimal totalTaxCollected,
                               BigDecimal grandTotal, long transactionCount,
                               long totalItemsSold) {
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.totalTaxCollected = totalTaxCollected != null ? totalTaxCollected : BigDecimal.ZERO;
        this.grandTotal = grandTotal != null ? grandTotal : BigDecimal.ZERO;
        this.transactionCount = transactionCount;
        this.totalItemsSold = totalItemsSold;
    }

    // --- Getters & Setters ---

    public BigDecimal getTotalRevenue() {
        return totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    public BigDecimal getTotalTaxCollected() {
        return totalTaxCollected != null ? totalTaxCollected : BigDecimal.ZERO;
    }

    public void setTotalTaxCollected(BigDecimal totalTaxCollected) {
        this.totalTaxCollected = totalTaxCollected != null ? totalTaxCollected : BigDecimal.ZERO;
    }

    // Alias for KPI format { taxCollected }
    public BigDecimal getTaxCollected() {
        return getTotalTaxCollected();
    }

    public void setTaxCollected(BigDecimal taxCollected) {
        this.totalTaxCollected = taxCollected != null ? taxCollected : BigDecimal.ZERO;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal != null ? grandTotal : BigDecimal.ZERO;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal != null ? grandTotal : BigDecimal.ZERO;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }

    // Alias for KPI format { totalTransactions }
    public long getTotalTransactions() {
        return transactionCount;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.transactionCount = totalTransactions;
    }

    public long getTotalItemsSold() {
        return totalItemsSold;
    }

    public void setTotalItemsSold(long totalItemsSold) {
        this.totalItemsSold = totalItemsSold;
    }

    // Alias for KPI format { itemsSold }
    public long getItemsSold() {
        return totalItemsSold;
    }

    public void setItemsSold(long itemsSold) {
        this.totalItemsSold = itemsSold;
    }
}
