package com.example.demo.repository;

import com.example.demo.model.SalesReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesReturnItemRepository extends JpaRepository<SalesReturnItem, SalesReturnItem.SalesReturnItemId> {

    List<SalesReturnItem> findBySalesReturnReturnId(Long returnId);
}
