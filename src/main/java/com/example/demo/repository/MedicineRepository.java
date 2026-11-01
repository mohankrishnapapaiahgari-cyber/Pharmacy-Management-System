package com.example.demo.repository;

import com.example.demo.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    /**
     * Search medicines by name or generic name (case-insensitive partial match).
     * Only returns medicines that have at least one non-expired batch with stock > 0.
     */
    @Query("""
           SELECT DISTINCT m FROM Medicine m
           JOIN BatchInventory b ON b.medicine = m
           WHERE (LOWER(m.medicineName) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR  LOWER(m.genericName)  LIKE LOWER(CONCAT('%', :keyword, '%')))
             AND b.quantityInStock > 0
             AND b.expiryDate >= CURRENT_DATE
           """)
    List<Medicine> searchAvailableMedicines(@Param("keyword") String keyword);
}
