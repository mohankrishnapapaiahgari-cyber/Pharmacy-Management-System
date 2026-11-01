package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "MANUFACTURER")
public class Manufacturer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manufacturer_id")
    private Long manufacturerId;

    @Column(name = "manufacturer_name", nullable = false)
    private String manufacturerName;

    @Column(name = "license_number", unique = true)
    private String licenseNumber;

    @Column(name = "contact_no")
    private String contactNo;

    @Column(name = "email")
    private String email;

    @Column(name = "street_address")
    private String streetAddress;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "postal_code")
    private String postalCode;

    // --- Constructors ---

    public Manufacturer() {
    }

    public Manufacturer(String manufacturerName, String licenseNumber, String contactNo,
                        String email, String streetAddress, String city, String state, String postalCode) {
        this.manufacturerName = manufacturerName;
        this.licenseNumber = licenseNumber;
        this.contactNo = contactNo;
        this.email = email;
        this.streetAddress = streetAddress;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
    }

    // --- Getters & Setters ---

    public Long getManufacturerId() {
        return manufacturerId;
    }

    public void setManufacturerId(Long manufacturerId) {
        this.manufacturerId = manufacturerId;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    // Backward-compatibility aliases
    public String getContactInfo() {
        return contactNo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactNo = contactInfo;
    }

    public String getAddress() {
        return streetAddress;
    }

    public void setAddress(String address) {
        this.streetAddress = address;
    }
}
