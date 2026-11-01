package com.example.demo.dto;

/**
 * Response DTO for prescription validation at checkout.
 */
public class PrescriptionValidationDTO {

    private boolean valid;
    private String message;
    private Long prescriptionId;

    public PrescriptionValidationDTO() {
    }

    public PrescriptionValidationDTO(boolean valid, String message, Long prescriptionId) {
        this.valid = valid;
        this.message = message;
        this.prescriptionId = prescriptionId;
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Long getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(Long prescriptionId) { this.prescriptionId = prescriptionId; }
}
