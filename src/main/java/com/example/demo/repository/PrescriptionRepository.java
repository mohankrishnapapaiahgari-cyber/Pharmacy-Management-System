package com.example.demo.repository;

import com.example.demo.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    @Query("SELECT p FROM Prescription p JOIN FETCH p.customer JOIN FETCH p.doctor WHERE p.customer.customerId = :customerId ORDER BY p.prescriptionDate DESC")
    List<Prescription> findByCustomerIdWithDetails(@Param("customerId") Long customerId);

    @Query("SELECT p FROM Prescription p JOIN FETCH p.customer JOIN FETCH p.doctor WHERE p.prescriptionId = :id")
    Optional<Prescription> findByIdWithDetails(@Param("id") Long id);
}
