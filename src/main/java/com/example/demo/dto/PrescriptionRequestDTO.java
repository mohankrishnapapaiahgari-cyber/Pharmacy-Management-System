package com.example.demo.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Request payload for creating a new prescription.
 */
public class PrescriptionRequestDTO {

    private Long customerId;
    private Long doctorId;
    private LocalDate prescriptionDate;
    private Integer validityPeriodDays;
    private String diagnosisNotes;
    private List<PrescriptionItemRequestDTO> items;

    public PrescriptionRequestDTO() {
    }

    // --- Getters & Setters ---

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getPrescriptionDate() {
        return prescriptionDate;
    }

    public void setPrescriptionDate(LocalDate prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
    }

    public Integer getValidityPeriodDays() {
        return validityPeriodDays;
    }

    public void setValidityPeriodDays(Integer validityPeriodDays) {
        this.validityPeriodDays = validityPeriodDays;
    }

    public String getDiagnosisNotes() {
        return diagnosisNotes;
    }

    public void setDiagnosisNotes(String diagnosisNotes) {
        this.diagnosisNotes = diagnosisNotes;
    }

    public List<PrescriptionItemRequestDTO> getItems() {
        return items;
    }

    public void setItems(List<PrescriptionItemRequestDTO> items) {
        this.items = items;
    }

    // ---- Nested line-item DTO ----
    public static class PrescriptionItemRequestDTO {
        private Long medicineId;
        private Integer quantityPrescribed;
        private String doseAmount;
        private String frequency;
        private String timing;
        private String duration;

        public PrescriptionItemRequestDTO() {
        }

        public Long getMedicineId() { return medicineId; }
        public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }
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
