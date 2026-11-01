package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SALES_RETURN")
public class SalesReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "return_id")
    private Long returnId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_employee_id", nullable = false)
    private Employee processedByEmployee;

    @Column(name = "return_date", nullable = false)
    private LocalDateTime returnDate;

    @Column(name = "reason", nullable = false)
    private String reason;

    // --- Constructors ---

    public SalesReturn() {
    }

    public SalesReturn(Invoice invoice, Employee processedByEmployee,
                       LocalDateTime returnDate, String reason) {
        this.invoice = invoice;
        this.processedByEmployee = processedByEmployee;
        this.returnDate = returnDate;
        this.reason = reason;
    }

    public SalesReturn(Long returnId, Invoice invoice, Employee processedByEmployee,
                       LocalDateTime returnDate, String reason) {
        this.returnId = returnId;
        this.invoice = invoice;
        this.processedByEmployee = processedByEmployee;
        this.returnDate = returnDate;
        this.reason = reason;
    }

    // --- Getters & Setters ---

    public Long getReturnId() {
        return returnId;
    }

    public void setReturnId(Long returnId) {
        this.returnId = returnId;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Employee getProcessedByEmployee() {
        return processedByEmployee;
    }

    public void setProcessedByEmployee(Employee processedByEmployee) {
        this.processedByEmployee = processedByEmployee;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
