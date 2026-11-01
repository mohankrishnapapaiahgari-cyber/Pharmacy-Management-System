package com.example.demo.repository;

import com.example.demo.model.BatchInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BatchInventoryRepository extends JpaRepository<BatchInventory, Long> {

    /**
     * FEFO query: returns all non-expired batches for a given medicine
     * that still have stock, ordered by earliest expiry first.
     */
    @Query("""
           SELECT b FROM BatchInventory b
           WHERE b.medicine.medicineId = :medicineId
             AND b.quantityInStock > 0
             AND b.expiryDate >= CURRENT_DATE
           ORDER BY b.expiryDate ASC
           """)
    List<BatchInventory> findBatchesByMedicineFEFO(@Param("medicineId") Long medicineId);

    /**
     * FEFO query with details: returns all non-expired batches for a given medicine
     * that still have stock, fetching medicine and shelf location, ordered by earliest expiry first.
     */
    @Query("""
           SELECT b FROM BatchInventory b
           JOIN FETCH b.medicine m
           LEFT JOIN FETCH b.location l
           WHERE b.medicine.medicineId = :medicineId
             AND b.quantityInStock > 0
             AND b.expiryDate >= CURRENT_DATE
           ORDER BY b.expiryDate ASC
           """)
    List<BatchInventory> findActiveBatchesByMedicineWithLocation(@Param("medicineId") Long medicineId);

    /**
     * Calculates the total available stock across all valid batches for a medicine.
     */
    @Query("""
           SELECT COALESCE(SUM(b.quantityInStock), 0)
           FROM BatchInventory b
           WHERE b.medicine.medicineId = :medicineId
             AND b.quantityInStock > 0
             AND b.expiryDate >= CURRENT_DATE
           """)
    int getTotalAvailableStock(@Param("medicineId") Long medicineId);

    /**
     * Expiring batch monitoring: returns batches expiring between today and maxDate
     * that still have stock, with medicine and shelf location details.
     */
    @Query("""
           SELECT b FROM BatchInventory b
           JOIN FETCH b.medicine m
           LEFT JOIN FETCH b.location l
           WHERE b.quantityInStock > 0
             AND b.expiryDate >= :today
             AND b.expiryDate <= :maxDate
           ORDER BY b.expiryDate ASC
           """)
    List<BatchInventory> findExpiringBatchesBetween(@Param("today") LocalDate today, @Param("maxDate") LocalDate maxDate);

    /**
     * Expiring batch monitoring backwards compatibility: returns batches expiring before cutoffDate.
     */
    default List<BatchInventory> findExpiringBatches(LocalDate cutoffDate) {
        return findExpiringBatchesBetween(LocalDate.now(), cutoffDate);
    }

    /**
     * Low-stock alert: returns batches where the total stock for a medicine
     * is at or below its reorder level. Uses a subquery to aggregate per medicine.
     */
    @Query("""
           SELECT b FROM BatchInventory b
           JOIN FETCH b.medicine m
           WHERE m.reorderLevel IS NOT NULL
             AND b.quantityInStock > 0
             AND b.expiryDate >= CURRENT_DATE
             AND (SELECT COALESCE(SUM(b2.quantityInStock), 0)
                  FROM BatchInventory b2
                  WHERE b2.medicine = m
                    AND b2.quantityInStock > 0
                    AND b2.expiryDate >= CURRENT_DATE) <= m.reorderLevel
           ORDER BY m.medicineName ASC
           """)
    List<BatchInventory> findLowStockBatches();

    /**
     * Returns all batch inventory records with medicine and shelf location details.
     */
    @Query("""
           SELECT b FROM BatchInventory b
           JOIN FETCH b.medicine m
           LEFT JOIN FETCH b.location l
           ORDER BY b.batchId DESC
           """)
    List<BatchInventory> findAllWithDetails();
}
