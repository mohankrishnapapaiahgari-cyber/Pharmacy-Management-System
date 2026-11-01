package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "SHELF_LOCATION")
public class ShelfLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "temperature_zone")
    private String temperatureZone;

    @Column(name = "aisle_number")
    private Integer aisleNumber;

    @Column(name = "shelf_number")
    private Integer shelfNumber;

    @Column(name = "rack_code")
    private String rackCode;

    // --- Constructors ---

    public ShelfLocation() {
    }

    public ShelfLocation(String temperatureZone, Integer aisleNumber, Integer shelfNumber, String rackCode) {
        this.temperatureZone = temperatureZone;
        this.aisleNumber = aisleNumber;
        this.shelfNumber = shelfNumber;
        this.rackCode = rackCode;
    }

    // --- Getters & Setters ---

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public String getTemperatureZone() {
        return temperatureZone;
    }

    public void setTemperatureZone(String temperatureZone) {
        this.temperatureZone = temperatureZone;
    }

    public Integer getAisleNumber() {
        return aisleNumber;
    }

    public void setAisleNumber(Integer aisleNumber) {
        this.aisleNumber = aisleNumber;
    }

    public Integer getShelfNumber() {
        return shelfNumber;
    }

    public void setShelfNumber(Integer shelfNumber) {
        this.shelfNumber = shelfNumber;
    }

    public String getRackCode() {
        return rackCode;
    }

    public void setRackCode(String rackCode) {
        this.rackCode = rackCode;
    }
}
