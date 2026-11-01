package com.example.demo.repository;

import com.example.demo.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminReportRepository extends JpaRepository<Medicine, Long> {

    /**
     * ADVANCED SQL 1: Window Function using RANK() OVER (ORDER BY SUM(ii.quantity) DESC).
     * Computes sales ranking across all medicines dynamically.
     */
    @Query(value = """
           SELECT m.medicine_id, m.medicine_name, m.generic_name,
                  COALESCE(SUM(ii.quantity), 0) AS units_sold,
                  COALESCE(SUM(ii.quantity * ii.unit_selling_price), 0) AS total_revenue,
                  RANK() OVER (ORDER BY COALESCE(SUM(ii.quantity), 0) DESC) AS sales_rank
           FROM MEDICINE m
           JOIN BATCH_INVENTORY b ON m.medicine_id = b.medicine_id
           JOIN INVOICE_ITEM ii ON b.batch_id = ii.batch_id
           GROUP BY m.medicine_id, m.medicine_name, m.generic_name
           ORDER BY sales_rank ASC
           """, nativeQuery = true)
    List<Object[]> getTopSellingMedicinesRanked();

    /**
     * Low stock alert query: returns active medicines where non-expired stock is <= reorder_level.
     * Includes category_name, current_stock, reorder_level, reorder_quantity, generic_name.
     */
    @Query(value = """
           SELECT m.medicine_id,
                  m.medicine_name,
                  COALESCE(c.category_name, 'General') AS category_name,
                  COALESCE((SELECT SUM(b.quantity_in_stock)
                            FROM BATCH_INVENTORY b
                            WHERE b.medicine_id = m.medicine_id
                              AND b.quantity_in_stock > 0
                              AND b.expiry_date >= CURRENT_DATE), 0) AS current_stock,
                  m.reorder_level,
                  m.reorder_quantity,
                  COALESCE(m.generic_name, '') AS generic_name
           FROM MEDICINE m
           LEFT JOIN CATEGORY c ON m.category_id = c.category_id
           WHERE m.is_active = TRUE
             AND COALESCE((SELECT SUM(b.quantity_in_stock)
                           FROM BATCH_INVENTORY b
                           WHERE b.medicine_id = m.medicine_id
                             AND b.quantity_in_stock > 0
                             AND b.expiry_date >= CURRENT_DATE), 0) <= m.reorder_level
           ORDER BY current_stock ASC, m.medicine_name ASC
           """, nativeQuery = true)
    List<Object[]> getLowStockAlertMedicines();

    /**
     * ADVANCED SQL 2: Correlated Subquery.
     * Identifies medicines whose current non-expired batch stock is at or below reorder level.
     */
    @Query(value = """
           SELECT m.medicine_id, m.medicine_name, m.generic_name, m.reorder_level,
                  COALESCE((SELECT SUM(b.quantity_in_stock)
                            FROM BATCH_INVENTORY b
                            WHERE b.medicine_id = m.medicine_id
                              AND b.expiry_date >= CURDATE()), 0) AS current_stock
           FROM MEDICINE m
           WHERE (SELECT COALESCE(SUM(b.quantity_in_stock), 0)
                  FROM BATCH_INVENTORY b
                  WHERE b.medicine_id = m.medicine_id
                    AND b.expiry_date >= CURDATE()) <= m.reorder_level
           ORDER BY current_stock ASC
           """, nativeQuery = true)
    List<Object[]> getLowStockByCorrelatedSubquery();

    /**
     * ADVANCED SQL 3: GROUP BY + HAVING.
     * Summarizes purchases per supplier having total order volume >= threshold.
     */
    @Query(value = """
           SELECT s.supplier_id, s.supplier_name, s.contact_person, s.phone,
                  COUNT(DISTINCT po.po_id) AS total_orders,
                  COALESCE(SUM(poi.quantity_ordered * poi.purchase_price), 0) AS total_purchase_amount
           FROM SUPPLIER s
           JOIN PURCHASE_ORDER po ON s.supplier_id = po.supplier_id
           JOIN PURCHASE_ORDER_ITEM poi ON po.po_id = poi.po_id
           GROUP BY s.supplier_id, s.supplier_name, s.contact_person, s.phone
           HAVING COALESCE(SUM(poi.quantity_ordered * poi.purchase_price), 0) >= :threshold
           ORDER BY total_purchase_amount DESC
           """, nativeQuery = true)
    List<Object[]> getSupplierPurchaseSummaryWithHaving(@Param("threshold") double threshold);

    /**
     * SQL VIEW Query: vw_medicine_stock_summary
     */
    @Query(value = """
           SELECT medicine_id, medicine_name, generic_name, category_name,
                  reorder_level, total_available_stock, stock_status
           FROM vw_medicine_stock_summary
           ORDER BY total_available_stock ASC
           """, nativeQuery = true)
    List<Object[]> getStockSummaryFromView();

    /**
     * Revenue by Category Query.
     */
    @Query(value = """
           SELECT c.category_id, c.category_name,
                  COALESCE(SUM(ii.quantity), 0) AS units_sold,
                  COALESCE(SUM(ii.unit_selling_price * ii.quantity), 0) AS category_revenue
           FROM CATEGORY c
           JOIN MEDICINE m ON c.category_id = m.category_id
           JOIN BATCH_INVENTORY b ON m.medicine_id = b.medicine_id
           JOIN INVOICE_ITEM ii ON b.batch_id = ii.batch_id
           GROUP BY c.category_id, c.category_name
           ORDER BY category_revenue DESC
           """, nativeQuery = true)
    List<Object[]> getRevenueByCategory();

    /**
     * Total Inventory Stock Valuation.
     */
    @Query(value = """
           SELECT COALESCE(SUM(b.quantity_in_stock * b.purchase_unit_cost), 0) AS total_valuation,
                  COALESCE(SUM(b.quantity_in_stock), 0) AS total_units,
                  COUNT(DISTINCT b.medicine_id) AS total_medicines
           FROM BATCH_INVENTORY b
           WHERE b.quantity_in_stock > 0
           """, nativeQuery = true)
    List<Object[]> getStockValuation();
}
