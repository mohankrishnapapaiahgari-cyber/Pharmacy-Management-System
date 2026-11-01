package com.example.demo.repository;

import com.example.demo.model.PrescriptionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, PrescriptionItem.PrescriptionItemId> {

    @Query("SELECT pi FROM PrescriptionItem pi JOIN FETCH pi.medicine WHERE pi.prescription.prescriptionId = :prescriptionId")
    List<PrescriptionItem> findByPrescriptionIdWithMedicine(@Param("prescriptionId") Long prescriptionId);

    @Query("SELECT pi FROM PrescriptionItem pi WHERE pi.prescription.prescriptionId = :prescriptionId AND pi.medicine.medicineId = :medicineId")
    List<PrescriptionItem> findByPrescriptionAndMedicine(@Param("prescriptionId") Long prescriptionId, @Param("medicineId") Long medicineId);
}
