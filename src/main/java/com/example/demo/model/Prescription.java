package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "PRESCRIPTION")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prescription_id")
    private Long prescriptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "prescription_date", nullable = false)
    private LocalDate prescriptionDate;

    @Column(name = "validity_period_days", nullable = false)
    private Integer validityPeriodDays = 30;

    @Column(name = "diagnosis_notes")
    private String diagnosisNotes;

    @Column(name = "prescription_image_url")
    private String prescriptionImageUrl;

    public Prescription() {
    }

    public Prescription(Customer customer, Doctor doctor, LocalDate prescriptionDate,
                        Integer validityPeriodDays, String diagnosisNotes, String prescriptionImageUrl) {
        this.customer = customer;
        this.doctor = doctor;
        this.prescriptionDate = prescriptionDate;
        this.validityPeriodDays = validityPeriodDays != null ? validityPeriodDays : 30;
        this.diagnosisNotes = diagnosisNotes;
        this.prescriptionImageUrl = prescriptionImageUrl;
    }

    public Long getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(Long prescriptionId) { this.prescriptionId = prescriptionId; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public LocalDate getPrescriptionDate() { return prescriptionDate; }
    public void setPrescriptionDate(LocalDate prescriptionDate) { this.prescriptionDate = prescriptionDate; }

    public Integer getValidityPeriodDays() { return validityPeriodDays; }
    public void setValidityPeriodDays(Integer validityPeriodDays) { this.validityPeriodDays = validityPeriodDays; }

    public String getDiagnosisNotes() { return diagnosisNotes; }
    public void setDiagnosisNotes(String diagnosisNotes) { this.diagnosisNotes = diagnosisNotes; }

    public String getPrescriptionImageUrl() { return prescriptionImageUrl; }
    public void setPrescriptionImageUrl(String prescriptionImageUrl) { this.prescriptionImageUrl = prescriptionImageUrl; }
}
