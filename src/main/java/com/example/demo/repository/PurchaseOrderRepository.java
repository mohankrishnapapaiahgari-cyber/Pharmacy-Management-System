package com.example.demo.repository;

import com.example.demo.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @Query("SELECT po FROM PurchaseOrder po JOIN FETCH po.supplier JOIN FETCH po.createdByEmployee ORDER BY po.orderDate DESC")
    List<PurchaseOrder> findAllWithDetails();

    @Query("SELECT po FROM PurchaseOrder po JOIN FETCH po.supplier JOIN FETCH po.createdByEmployee WHERE po.orderStatus = :status ORDER BY po.orderDate DESC")
    List<PurchaseOrder> findByOrderStatusWithDetails(String status);

    @Query("SELECT po FROM PurchaseOrder po JOIN FETCH po.supplier JOIN FETCH po.createdByEmployee WHERE po.poId = :poId")
    java.util.Optional<PurchaseOrder> findByIdWithDetails(@org.springframework.data.repository.query.Param("poId") Long poId);
}
