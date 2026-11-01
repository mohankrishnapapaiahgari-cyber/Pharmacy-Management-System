package com.example.demo.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "MEDICINE_SUPPLIER")
@IdClass(MedicineSupplier.MedicineSupplierId.class)
public class MedicineSupplier {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "supply_price", precision = 10, scale = 2)
    private BigDecimal supplyPrice;

    @Column(name = "lead_time_days")
    private Integer leadTimeDays;

    // --- Composite Key Class ---

    public static class MedicineSupplierId implements Serializable {
        private Long medicine;
        private Long supplier;

        public MedicineSupplierId() {
        }

        public MedicineSupplierId(Long medicine, Long supplier) {
            this.medicine = medicine;
            this.supplier = supplier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MedicineSupplierId)) return false;
            MedicineSupplierId that = (MedicineSupplierId) o;
            return Objects.equals(medicine, that.medicine) && Objects.equals(supplier, that.supplier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(medicine, supplier);
        }
    }

    // --- Constructors ---

    public MedicineSupplier() {
    }

    // --- Getters & Setters ---

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public BigDecimal getSupplyPrice() {
        return supplyPrice;
    }

    public void setSupplyPrice(BigDecimal supplyPrice) {
        this.supplyPrice = supplyPrice;
    }

    public Integer getLeadTimeDays() {
        return leadTimeDays;
    }

    public void setLeadTimeDays(Integer leadTimeDays) {
        this.leadTimeDays = leadTimeDays;
    }
}
