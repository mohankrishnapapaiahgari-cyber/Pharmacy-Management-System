package com.example.demo.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "PRESCRIPTION_ITEM")
@IdClass(PrescriptionItem.PrescriptionItemId.class)
public class PrescriptionItem {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(name = "quantity_prescribed", nullable = false)
    private Integer quantityPrescribed;

    @Column(name = "dose_amount")
    private String doseAmount;

    @Column(name = "frequency")
    private String frequency;

    @Column(name = "timing")
    private String timing;

    @Column(name = "duration")
    private String duration;

    public PrescriptionItem() {}

    // All getters and setters
    public Prescription getPrescription() { return prescription; }
    public void setPrescription(Prescription prescription) { this.prescription = prescription; }
    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }
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

    // Composite PK class
    public static class PrescriptionItemId implements Serializable {
        private Long prescription;
        private Long medicine;
        public PrescriptionItemId() {}
        public PrescriptionItemId(Long prescription, Long medicine) {
            this.prescription = prescription;
            this.medicine = medicine;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PrescriptionItemId that = (PrescriptionItemId) o;
            return Objects.equals(prescription, that.prescription) && Objects.equals(medicine, that.medicine);
        }
        @Override
        public int hashCode() { return Objects.hash(prescription, medicine); }
    }
}
