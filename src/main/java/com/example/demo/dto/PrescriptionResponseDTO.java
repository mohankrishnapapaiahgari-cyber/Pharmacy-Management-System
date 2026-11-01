package com.example.demo.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for prescription data with computed validity status.
 */
public class PrescriptionResponseDTO {

    private Long prescriptionId;
    private String customerName;
    private Long customerId;
    private String doctorName;
    private String doctorLicense;
    private LocalDate prescriptionDate;
    private Integer validityPeriodDays;
    private LocalDate expiryDate;
    private String status; // "Active" or "Expired"
    private String diagnosisNotes;
    private List<PrescriptionItemDTO> items;

    // --- Getters & Setters ---

    public Long getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(Long prescriptionId) { this.prescriptionId = prescriptionId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getDoctorLicense() { return doctorLicense; }
    public void setDoctorLicense(String doctorLicense) { this.doctorLicense = doctorLicense; }
    public LocalDate getPrescriptionDate() { return prescriptionDate; }
    public void setPrescriptionDate(LocalDate prescriptionDate) { this.prescriptionDate = prescriptionDate; }
    public Integer getValidityPeriodDays() { return validityPeriodDays; }
    public void setValidityPeriodDays(Integer validityPeriodDays) { this.validityPeriodDays = validityPeriodDays; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDiagnosisNotes() { return diagnosisNotes; }
    public void setDiagnosisNotes(String diagnosisNotes) { this.diagnosisNotes = diagnosisNotes; }
    public List<PrescriptionItemDTO> getItems() { return items; }
    public void setItems(List<PrescriptionItemDTO> items) { this.items = items; }

    // ---- Nested line-item DTO ----
    public static class PrescriptionItemDTO {
        private Long medicineId;
        private String medicineName;
        private Integer quantityPrescribed;
        private String doseAmount;
        private String frequency;
        private String timing;
        private String duration;

        public Long getMedicineId() { return medicineId; }
        public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }
        public String getMedicineName() { return medicineName; }
        public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
        public Integer getQuantityPrescribed() { return quantityPrescribed; }
        public void setQuantityPrescribed(Integer quantityPrescribed) { this.quantityPrescribed = quantityPrescribed; }
        public String getDoseAmount() { return doseAmount; }
        public void setDoseAmount(String doseAmount) { this.doseAmount = doseAmount; }
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        public String getTiming() { return timing; }
        public void setTiming(String timing) { this.timing = timing; }
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
    }
}
