package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "CATEGORY")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name", nullable = false, unique = true)
    private String categoryName;

    @Column(name = "description")
    private String description;

    @Column(name = "requires_temperature_control")
    private Boolean requiresTemperatureControl = false;

    // --- Constructors ---

    public Category() {
    }

    public Category(String categoryName, String description) {
        this.categoryName = categoryName;
        this.description = description;
        this.requiresTemperatureControl = false;
    }

    public Category(String categoryName, String description, Boolean requiresTemperatureControl) {
        this.categoryName = categoryName;
        this.description = description;
        this.requiresTemperatureControl = requiresTemperatureControl;
    }

    // --- Getters & Setters ---

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getRequiresTemperatureControl() {
        return requiresTemperatureControl;
    }

    public void setRequiresTemperatureControl(Boolean requiresTemperatureControl) {
        this.requiresTemperatureControl = requiresTemperatureControl;
    }
}
