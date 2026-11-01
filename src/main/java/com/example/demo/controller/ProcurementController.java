package com.example.demo.controller;

import com.example.demo.dto.BatchInventoryDTO;
import com.example.demo.dto.BulkStockIntakeRequestDTO;
import com.example.demo.dto.PurchaseOrderRequestDTO;
import com.example.demo.dto.PurchaseOrderResponseDTO;
import com.example.demo.dto.StockIntakeRequestDTO;
import com.example.demo.model.ShelfLocation;
import com.example.demo.model.Supplier;
import com.example.demo.service.ProcurementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procurement")
public class ProcurementController {

    private final ProcurementService procurementService;

    public ProcurementController(ProcurementService procurementService) {
        this.procurementService = procurementService;
    }

    // GET /api/procurement/suppliers
    @GetMapping("/suppliers")
    public ResponseEntity<List<Supplier>> getAllSuppliers() {
        return ResponseEntity.ok(procurementService.getAllSuppliers());
    }

    // GET /api/procurement/locations
    @GetMapping("/locations")
    public ResponseEntity<List<ShelfLocation>> getAllLocations() {
        return ResponseEntity.ok(procurementService.getAllLocations());
    }

    // POST /api/procurement/purchase-orders
    @PostMapping("/purchase-orders")
    public ResponseEntity<PurchaseOrderResponseDTO> createPurchaseOrder(
            @RequestBody PurchaseOrderRequestDTO request) {
        try {
            PurchaseOrderResponseDTO response = procurementService.createPurchaseOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // GET /api/procurement/purchase-orders?status=PENDING
    @GetMapping("/purchase-orders")
    public ResponseEntity<List<PurchaseOrderResponseDTO>> getPurchaseOrders(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(procurementService.getPurchaseOrders(status));
    }

    // POST /api/procurement/receive-stock  (single item)
    @PostMapping("/receive-stock")
    public ResponseEntity<?> receiveStock(@RequestBody StockIntakeRequestDTO request) {
        try {
            PurchaseOrderResponseDTO response = procurementService.receiveShipment(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // POST /api/procurement/receive-all-stock  (bulk — all items at once)
    @PostMapping("/receive-all-stock")
    public ResponseEntity<?> receiveAllStock(@RequestBody BulkStockIntakeRequestDTO request) {
        try {
            PurchaseOrderResponseDTO response = procurementService.receiveShipmentBulk(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT /api/procurement/purchase-orders/{poId}/cancel
    @PutMapping("/purchase-orders/{poId}/cancel")
    public ResponseEntity<?> cancelPurchaseOrder(@PathVariable Long poId) {
        try {
            PurchaseOrderResponseDTO response = procurementService.cancelPurchaseOrder(poId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET /api/procurement/inventory  (Received Stock & Shelf Allocation Log)
    @GetMapping("/inventory")
    public ResponseEntity<List<BatchInventoryDTO>> getReceivedInventoryLog() {
        return ResponseEntity.ok(procurementService.getReceivedInventoryLog());
    }
}

