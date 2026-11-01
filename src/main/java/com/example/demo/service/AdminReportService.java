package com.example.demo.service;

import com.example.demo.dto.ExpiringBatchDTO;
import com.example.demo.dto.FinancialSummaryDTO;
import com.example.demo.dto.LowStockAlertDTO;
import com.example.demo.model.BatchInventory;
import com.example.demo.model.ShelfLocation;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminReportService {

    private final BatchInventoryRepository batchInventoryRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final InvoiceRepository invoiceRepository;
    private final AdminReportRepository adminReportRepository;
    private final MedicineRepository medicineRepository;
    private final EmployeeRepository employeeRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public AdminReportService(BatchInventoryRepository batchInventoryRepository,
                              InvoiceItemRepository invoiceItemRepository,
                              InvoiceRepository invoiceRepository,
                              AdminReportRepository adminReportRepository,
                              MedicineRepository medicineRepository,
                              EmployeeRepository employeeRepository,
                              PurchaseOrderRepository purchaseOrderRepository) {
        this.batchInventoryRepository = batchInventoryRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.invoiceRepository = invoiceRepository;
        this.adminReportRepository = adminReportRepository;
        this.medicineRepository = medicineRepository;
        this.employeeRepository = employeeRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    // =========================================================
    // 1. LOW-STOCK ALERTS (Service Aggregate)
    // =========================================================
    @Transactional(readOnly = true)
    public List<LowStockAlertDTO> getLowStockAlerts() {
        List<Object[]> rows = adminReportRepository.getLowStockAlertMedicines();
        List<LowStockAlertDTO> list = new ArrayList<>();

        for (Object[] r : rows) {
            LowStockAlertDTO dto = new LowStockAlertDTO();
            dto.setMedicineId(toLong(r[0]));
            dto.setMedicineName(String.valueOf(r[1]));
            dto.setCategoryName(r[2] != null ? String.valueOf(r[2]) : "General");
            dto.setCurrentStock((int) toLong(r[3]));
            dto.setReorderLevel(r[4] != null ? (int) toLong(r[4]) : 0);
            dto.setReorderQuantity(r[5] != null ? (int) toLong(r[5]) : 0);
            dto.setGenericName(r[6] != null ? String.valueOf(r[6]) : "");
            list.add(dto);
        }

        return list;
    }

    // =========================================================
    // 2. EXPIRING BATCH MONITORING
    // =========================================================
    @Transactional(readOnly = true)
    public List<ExpiringBatchDTO> getExpiringBatches(int days) {
        int d = days > 0 ? days : 60;
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(d);
        List<BatchInventory> batches = batchInventoryRepository.findExpiringBatchesBetween(today, maxDate);

        return batches.stream().map(batch -> {
            long daysUntil = ChronoUnit.DAYS.between(today, batch.getExpiryDate());
            String shelfLocation = "Unassigned";
            Integer aisleNumber = null;
            String rackCode = null;
            Integer shelfNumber = null;

            if (batch.getLocation() != null) {
                ShelfLocation loc = batch.getLocation();
                shelfLocation = MedicineService.formatShelfLocation(loc);
                aisleNumber = loc.getAisleNumber();
                rackCode = loc.getRackCode();
                shelfNumber = loc.getShelfNumber();
            }
            return new ExpiringBatchDTO(
                    batch.getBatchId(),
                    batch.getBatchNumber(),
                    batch.getMedicine() != null ? batch.getMedicine().getMedicineId() : null,
                    batch.getMedicine() != null ? batch.getMedicine().getMedicineName() : "Unknown",
                    batch.getQuantityInStock(),
                    batch.getExpiryDate(),
                    daysUntil,
                    shelfLocation,
                    aisleNumber,
                    rackCode,
                    shelfNumber
            );
        }).collect(Collectors.toList());
    }

    // =========================================================
    // 3. FINANCIAL SUMMARY
    // =========================================================
    @Transactional(readOnly = true)
    public FinancialSummaryDTO getFinancialSummary() {
        List<Object[]> results = invoiceItemRepository.getFinancialSummary();
        FinancialSummaryDTO dto = new FinancialSummaryDTO();

        if (results != null && !results.isEmpty() && results.get(0) != null) {
            Object[] row = results.get(0);
            BigDecimal revenue = toBigDecimal(row[0]);
            BigDecimal tax = toBigDecimal(row[1]).setScale(2, RoundingMode.HALF_UP);
            long txnCount = toLong(row[2]);
            long itemsSold = toLong(row[3]);

            dto.setTotalRevenue(revenue);
            dto.setTotalTaxCollected(tax);
            dto.setGrandTotal(revenue.add(tax));
            dto.setTransactionCount(txnCount);
            dto.setTotalItemsSold(itemsSold);
        } else {
            dto.setTotalRevenue(BigDecimal.ZERO);
            dto.setTotalTaxCollected(BigDecimal.ZERO);
            dto.setGrandTotal(BigDecimal.ZERO);
            dto.setTransactionCount(0L);
            dto.setTotalItemsSold(0L);
        }

        return dto;
    }

    // =========================================================
    // EXECUTIVE DASHBOARD KPIS
    // =========================================================
    @Transactional(readOnly = true)
    public Map<String, Object> getExecutiveKPIs() {
        FinancialSummaryDTO fin = getFinancialSummary();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("totalRevenue", fin.getTotalRevenue());
        map.put("taxCollected", fin.getTaxCollected());
        map.put("totalTaxCollected", fin.getTotalTaxCollected());
        map.put("totalTransactions", fin.getTotalTransactions());
        map.put("transactionCount", fin.getTransactionCount());
        map.put("itemsSold", fin.getItemsSold());
        map.put("totalItemsSold", fin.getTotalItemsSold());
        map.put("grandTotal", fin.getGrandTotal());
        return map;
    }

    // =========================================================
    // 4. TOP-SELLING MEDICINES (WINDOW FUNCTION: RANK() OVER)
    // =========================================================
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopSellingMedicinesRanked() {
        List<Object[]> rows = adminReportRepository.getTopSellingMedicinesRanked();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("medicineId", toLong(r[0]));
            map.put("medicineName", String.valueOf(r[1]));
            map.put("genericName", r[2] != null ? String.valueOf(r[2]) : "");
            map.put("unitsSold", toLong(r[3]));
            map.put("totalRevenue", toBigDecimal(r[4]));
            map.put("salesRank", toLong(r[5]));
            list.add(map);
        }
        return list;
    }

    // =========================================================
    // 5. CORRELATED SUBQUERY LOW-STOCK REPORT
    // =========================================================
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getLowStockByCorrelatedSubquery() {
        List<Object[]> rows = adminReportRepository.getLowStockByCorrelatedSubquery();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("medicineId", toLong(r[0]));
            map.put("medicineName", String.valueOf(r[1]));
            map.put("genericName", r[2] != null ? String.valueOf(r[2]) : "");
            map.put("reorderLevel", toLong(r[3]));
            map.put("currentStock", toLong(r[4]));
            list.add(map);
        }
        return list;
    }

    // =========================================================
    // 6. SUPPLIER SUMMARY (GROUP BY + HAVING)
    // =========================================================
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSupplierPurchaseSummary(double threshold) {
        List<Object[]> rows = adminReportRepository.getSupplierPurchaseSummaryWithHaving(threshold);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("supplierId", toLong(r[0]));
            map.put("supplierName", String.valueOf(r[1]));
            map.put("contactPerson", r[2] != null ? String.valueOf(r[2]) : "");
            map.put("phone", r[3] != null ? String.valueOf(r[3]) : "");
            map.put("totalOrders", toLong(r[4]));
            map.put("totalPurchaseAmount", toBigDecimal(r[5]));
            list.add(map);
        }
        return list;
    }

    // =========================================================
    // 7. STOCK SUMMARY FROM SQL VIEW (vw_medicine_stock_summary)
    // =========================================================
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStockSummaryFromView() {
        List<Object[]> rows = adminReportRepository.getStockSummaryFromView();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            Long medId = toLong(r[0]);
            String medName = String.valueOf(r[1]);
            String genName = r[2] != null ? String.valueOf(r[2]) : "";
            String catName = r[3] != null ? String.valueOf(r[3]) : "";
            Long reorderLvl = toLong(r[4]);
            Long stock = toLong(r[5]);
            String status = String.valueOf(r[6]);

            // camelCase keys
            map.put("medicineId", medId);
            map.put("medicineName", medName);
            map.put("genericName", genName);
            map.put("categoryName", catName);
            map.put("reorderLevel", reorderLvl);
            map.put("totalAvailableStock", stock);
            map.put("totalStock", stock);
            map.put("stockStatus", status);

            // snake_case keys for compatibility
            map.put("medicine_id", medId);
            map.put("medicine_name", medName);
            map.put("generic_name", genName);
            map.put("category_name", catName);
            map.put("reorder_level", reorderLvl);
            map.put("total_available_stock", stock);
            map.put("total_quantity_in_stock", stock);
            map.put("stock_status", status);

            list.add(map);
        }
        return list;
    }

    // =========================================================
    // 8. REVENUE BY CATEGORY
    // =========================================================
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRevenueByCategory() {
        List<Object[]> rows = adminReportRepository.getRevenueByCategory();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("categoryId", toLong(r[0]));
            map.put("categoryName", String.valueOf(r[1]));
            map.put("unitsSold", toLong(r[2]));
            map.put("categoryRevenue", toBigDecimal(r[3]));
            list.add(map);
        }
        return list;
    }

    // =========================================================
    // 9. STOCK VALUATION REPORT
    // =========================================================
    @Transactional(readOnly = true)
    public Map<String, Object> getStockValuation() {
        List<Object[]> rows = adminReportRepository.getStockValuation();
        Map<String, Object> map = new LinkedHashMap<>();
        if (rows != null && !rows.isEmpty() && rows.get(0) != null) {
            Object[] r = rows.get(0);
            map.put("totalValuation", toBigDecimal(r[0]));
            map.put("totalUnits", toLong(r[1]));
            map.put("totalMedicines", toLong(r[2]));
        } else {
            map.put("totalValuation", BigDecimal.ZERO);
            map.put("totalUnits", 0L);
            map.put("totalMedicines", 0L);
        }
        return map;
    }

    // =========================================================
    // 10. OWNER DASHBOARD KPIS
    // =========================================================
    @Transactional(readOnly = true)
    public Map<String, Object> getOwnerDashboardKPIs() {
        FinancialSummaryDTO fin = getFinancialSummary();
        long totalMedicines = medicineRepository.count();
        long totalEmployees = employeeRepository.count();
        long pendingPOs = 0;
        try {
            pendingPOs = purchaseOrderRepository.findByOrderStatusWithDetails("PENDING").size();
        } catch (Exception ignored) {
        }

        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("totalRevenue", fin.getTotalRevenue());
        kpi.put("taxCollected", fin.getTaxCollected());
        kpi.put("totalTaxCollected", fin.getTotalTaxCollected());
        kpi.put("totalTransactions", fin.getTotalTransactions());
        kpi.put("itemsSold", fin.getItemsSold());
        kpi.put("grandTotal", fin.getGrandTotal());
        kpi.put("totalMedicines", totalMedicines);
        kpi.put("totalEmployees", totalEmployees);
        kpi.put("pendingPurchaseOrders", pendingPOs);
        return kpi;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            return 0L;
        }
    }
}
