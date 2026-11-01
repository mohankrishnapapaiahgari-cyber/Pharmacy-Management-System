package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "DOCTOR")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doctor_id")
    private Long doctorId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "medical_license_no", nullable = false, unique = true)
    private String medicalLicenseNo;

    @Column(name = "phone")
    private String phone;

    @Column(name = "hospital_clinic")
    private String hospitalClinic;

    @Column(name = "specialization")
    private String specialization;

    public Doctor() {}

    public Doctor(String name, String medicalLicenseNo, String phone, String hospitalClinic, String specialization) {
        this.name = name;
        this.medicalLicenseNo = medicalLicenseNo;
        this.phone = phone;
        this.hospitalClinic = hospitalClinic;
        this.specialization = specialization;
    }

    // All getters and setters for all 6 fields
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMedicalLicenseNo() { return medicalLicenseNo; }
    public void setMedicalLicenseNo(String medicalLicenseNo) { this.medicalLicenseNo = medicalLicenseNo; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getHospitalClinic() { return hospitalClinic; }
    public void setHospitalClinic(String hospitalClinic) { this.hospitalClinic = hospitalClinic; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
}
