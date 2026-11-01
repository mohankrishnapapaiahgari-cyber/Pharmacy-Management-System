package com.example.demo.repository;

import com.example.demo.model.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {

    @Query("SELECT poi FROM PurchaseOrderItem poi JOIN FETCH poi.medicine WHERE poi.purchaseOrder.poId = :poId")
    List<PurchaseOrderItem> findByPurchaseOrderId(Long poId);
}
