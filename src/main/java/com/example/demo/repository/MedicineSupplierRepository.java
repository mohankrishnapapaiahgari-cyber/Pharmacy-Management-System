package com.example.demo.repository;

import com.example.demo.model.MedicineSupplier;
import com.example.demo.model.MedicineSupplier.MedicineSupplierId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicineSupplierRepository extends JpaRepository<MedicineSupplier, MedicineSupplierId> {
}
