package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * The full checkout payload sent from the Cashier POS frontend.
 */
public class CheckoutRequestDTO {

    private Long customerId;        // nullable for walk-in customers
    private Long employeeId;        // cashier's employee ID
    private Long prescriptionId;    // nullable; required if cart contains Schedule H drugs
    private List<CheckoutItemDTO> items;
    private BigDecimal discountAmount;  // optional discount
    private String paymentMethod;       // 'CASH', 'UPI', 'CARD', 'NET_BANKING'
    private String transactionReference; // optional for CASH, mandatory for electronic

    public CheckoutRequestDTO() {
    }

    public CheckoutRequestDTO(Long customerId, Long employeeId, Long prescriptionId,
                               List<CheckoutItemDTO> items, BigDecimal discountAmount,
                               String paymentMethod, String transactionReference) {
        this.customerId = customerId;
        this.employeeId = employeeId;
        this.prescriptionId = prescriptionId;
        this.items = items;
        this.discountAmount = discountAmount;
        this.paymentMethod = paymentMethod;
        this.transactionReference = transactionReference;
    }

    // --- Getters & Setters ---

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Long getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(Long prescriptionId) { this.prescriptionId = prescriptionId; }

    public List<CheckoutItemDTO> getItems() { return items; }
    public void setItems(List<CheckoutItemDTO> items) { this.items = items; }

    public BigDecimal getDiscountAmount() { return discountAmount != null ? discountAmount : BigDecimal.ZERO; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
}
