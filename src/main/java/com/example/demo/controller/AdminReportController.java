package com.example.demo.controller;

import com.example.demo.dto.ExpiringBatchDTO;
import com.example.demo.dto.FinancialSummaryDTO;
import com.example.demo.dto.LowStockAlertDTO;
import com.example.demo.service.AdminReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/reports", "/api/dashboard", "/api/admin/reports"})
public class AdminReportController {

    private final AdminReportService adminReportService;

    public AdminReportController(AdminReportService adminReportService) {
        this.adminReportService = adminReportService;
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<LowStockAlertDTO>> getLowStockAlerts() {
        return ResponseEntity.ok(adminReportService.getLowStockAlerts());
    }

    @GetMapping({"/expiring-batches", "/expiring"})
    public ResponseEntity<List<ExpiringBatchDTO>> getExpiringBatches(
            @RequestParam(name = "days", defaultValue = "60") int days) {
        return ResponseEntity.ok(adminReportService.getExpiringBatches(days));
    }

    @GetMapping({"/kpis", "/financial-summary"})
    public ResponseEntity<FinancialSummaryDTO> getFinancialSummary() {
        return ResponseEntity.ok(adminReportService.getFinancialSummary());
    }

    // ADVANCED SQL 1: Window Function Rank
    @GetMapping("/top-selling")
    public ResponseEntity<List<Map<String, Object>>> getTopSellingMedicines() {
        return ResponseEntity.ok(adminReportService.getTopSellingMedicinesRanked());
    }

    // ADVANCED SQL 2: Correlated Subquery
    @GetMapping("/low-stock-subquery")
    public ResponseEntity<List<Map<String, Object>>> getLowStockSubquery() {
        return ResponseEntity.ok(adminReportService.getLowStockByCorrelatedSubquery());
    }

    // ADVANCED SQL 3: GROUP BY + HAVING
    @GetMapping("/supplier-summary")
    public ResponseEntity<List<Map<String, Object>>> getSupplierSummary(
            @RequestParam(name = "threshold", defaultValue = "0.0") double threshold) {
        return ResponseEntity.ok(adminReportService.getSupplierPurchaseSummary(threshold));
    }

    // SQL VIEW: vw_medicine_stock_summary
    @GetMapping({"/stock-summary-view", "/stock-summary"})
    public ResponseEntity<List<Map<String, Object>>> getStockSummaryView() {
        return ResponseEntity.ok(adminReportService.getStockSummaryFromView());
    }

    // Category Revenue Breakdown
    @GetMapping("/revenue-by-category")
    public ResponseEntity<List<Map<String, Object>>> getRevenueByCategory() {
        return ResponseEntity.ok(adminReportService.getRevenueByCategory());
    }

    // Inventory Stock Valuation
    @GetMapping("/stock-valuation")
    public ResponseEntity<Map<String, Object>> getStockValuation() {
        return ResponseEntity.ok(adminReportService.getStockValuation());
    }

    // Owner Dashboard KPIs
    @GetMapping("/owner-kpis")
    public ResponseEntity<Map<String, Object>> getOwnerKPIs() {
        return ResponseEntity.ok(adminReportService.getOwnerDashboardKPIs());
    }
}
