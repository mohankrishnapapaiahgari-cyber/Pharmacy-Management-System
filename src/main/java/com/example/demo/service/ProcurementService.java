package com.example.demo.service;

import com.example.demo.dto.BatchInventoryDTO;
import com.example.demo.dto.BulkStockIntakeRequestDTO;
import com.example.demo.dto.PurchaseOrderRequestDTO;
import com.example.demo.dto.PurchaseOrderResponseDTO;
import com.example.demo.dto.StockIntakeRequestDTO;
import com.example.demo.exception.PharmacyBusinessException;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProcurementService {

    private final SupplierRepository supplierRepository;
    private final ShelfLocationRepository shelfLocationRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final MedicineRepository medicineRepository;
    private final EmployeeRepository employeeRepository;
    private final BatchInventoryRepository batchInventoryRepository;

    public ProcurementService(SupplierRepository supplierRepository,
                               ShelfLocationRepository shelfLocationRepository,
                               PurchaseOrderRepository purchaseOrderRepository,
                               PurchaseOrderItemRepository purchaseOrderItemRepository,
                               MedicineRepository medicineRepository,
                               EmployeeRepository employeeRepository,
                               BatchInventoryRepository batchInventoryRepository) {
        this.supplierRepository = supplierRepository;
        this.shelfLocationRepository = shelfLocationRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.medicineRepository = medicineRepository;
        this.employeeRepository = employeeRepository;
        this.batchInventoryRepository = batchInventoryRepository;
    }

    // --- GET all suppliers ---
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    // --- GET all shelf locations ---
    public List<ShelfLocation> getAllLocations() {
        return shelfLocationRepository.findAll();
    }

    // --- CREATE Purchase Order (PENDING status) ---
    @Transactional(rollbackFor = Exception.class)
    public PurchaseOrderResponseDTO createPurchaseOrder(PurchaseOrderRequestDTO request) {
        // E23: Supplier doesn't exist
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new PharmacyBusinessException("E23", "Supplier not found with ID: " + request.getSupplierId()));

        // E24: Employee doesn't exist
        Employee employee = employeeRepository.findById(request.getCreatedByEmployeeId())
                .orElseThrow(() -> new PharmacyBusinessException("E24", "Employee not found with ID: " + request.getCreatedByEmployeeId()));

        // E28: Delivery date validation
        if (request.getExpectedDeliveryDate() != null && request.getExpectedDeliveryDate().isBefore(LocalDate.now())) {
            throw new PharmacyBusinessException("E28", "Expected delivery date cannot be in the past.");
        }

        PurchaseOrder po = new PurchaseOrder();
        po.setSupplier(supplier);
        po.setCreatedByEmployee(employee);
        po.setOrderDate(LocalDate.now());
        po.setOrderStatus("PENDING");
        po.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        po = purchaseOrderRepository.save(po);

        final PurchaseOrder savedPo = po;

        if (request.getItems() != null) {
            for (PurchaseOrderRequestDTO.PurchaseOrderItemDTO itemDTO : request.getItems()) {
                // E26: Ordered qty <= 0
                if (itemDTO.getQuantityOrdered() == null || itemDTO.getQuantityOrdered() <= 0) {
                    throw new PharmacyBusinessException("E26", "Ordered quantity must be greater than zero.");
                }

                Medicine medicine = medicineRepository.findById(itemDTO.getMedicineId())
                        .orElseThrow(() -> new PharmacyBusinessException("E01", "Medicine not found with ID: " + itemDTO.getMedicineId()));

                PurchaseOrderItem item = new PurchaseOrderItem();
                item.setPurchaseOrder(savedPo);
                item.setMedicine(medicine);
                item.setQuantityOrdered(itemDTO.getQuantityOrdered());
                item.setQuantityReceived(0);
                item.setPurchasePrice(itemDTO.getPurchasePrice());
                purchaseOrderItemRepository.save(item);
            }
        }

        return buildResponseDTO(savedPo);
    }

    // --- GET Purchase Orders (optionally filtered by status) ---
    public List<PurchaseOrderResponseDTO> getPurchaseOrders(String status) {
        List<PurchaseOrder> orders;
        if (status != null && !status.isBlank()) {
            orders = purchaseOrderRepository.findByOrderStatusWithDetails(status.toUpperCase());
        } else {
            orders = purchaseOrderRepository.findAllWithDetails();
        }
        return orders.stream().map(this::buildResponseDTO).collect(Collectors.toList());
    }

    // --- RECEIVE STOCK: single medicine intake ---
    @Transactional(rollbackFor = Exception.class)
    public PurchaseOrderResponseDTO receiveShipment(StockIntakeRequestDTO request) {
        PurchaseOrder po = purchaseOrderRepository.findByIdWithDetails(request.getPoId())
                .orElseThrow(() -> new PharmacyBusinessException("E29", "Purchase Order #" + request.getPoId() + " not found."));

        // E29: Receiving against CANCELLED or COMPLETED PO
        if ("CANCELLED".equalsIgnoreCase(po.getOrderStatus())) {
            throw new PharmacyBusinessException("E29", "Cannot receive stock against a CANCELLED purchase order.");
        }
        if ("COMPLETED".equalsIgnoreCase(po.getOrderStatus()) || "RECEIVED".equalsIgnoreCase(po.getOrderStatus())) {
            throw new PharmacyBusinessException("E29", "Purchase Order #" + po.getPoId() + " is already fully received.");
        }

        // Validations
        if (request.getQuantityReceived() == null || request.getQuantityReceived() <= 0) {
            throw new PharmacyBusinessException("E26", "Quantity received must be greater than zero.");
        }
        if (request.getBatchNumber() == null || request.getBatchNumber().isBlank()) {
            throw new PharmacyBusinessException("E31", "Batch number is required.");
        }
        if (request.getExpiryDate() == null) {
            throw new PharmacyBusinessException("E32", "Expiry date is required.");
        }
        // E08: Expiry date in the past
        if (request.getExpiryDate().isBefore(LocalDate.now())) {
            throw new PharmacyBusinessException("E08", "Expiry date cannot be in the past: " + request.getExpiryDate());
        }
        // E09: Mfg date after expiry date
        if (request.getMfgDate() != null && request.getMfgDate().isAfter(request.getExpiryDate())) {
            throw new PharmacyBusinessException("E09", "Manufacturing date cannot be after expiry date.");
        }

        Medicine medicine = medicineRepository.findById(request.getMedicineId())
                .orElseThrow(() -> new PharmacyBusinessException("E01", "Medicine not found with ID: " + request.getMedicineId()));

        List<PurchaseOrderItem> items = purchaseOrderItemRepository.findByPurchaseOrderId(po.getPoId());
        PurchaseOrderItem matchingItem = items.stream()
                .filter(i -> i.getMedicine().getMedicineId().equals(request.getMedicineId()))
                .findFirst()
                .orElseThrow(() -> new PharmacyBusinessException("E25",
                        "Medicine ID " + request.getMedicineId() + " not found in PO #" + po.getPoId()));

        int previousReceived = matchingItem.getQuantityReceived() == null ? 0 : matchingItem.getQuantityReceived();
        int newTotalReceived = previousReceived + request.getQuantityReceived();

        // E30: Over-delivery check
        if (newTotalReceived > matchingItem.getQuantityOrdered()) {
            throw new PharmacyBusinessException("E30",
                    "Received quantity (" + newTotalReceived + ") exceeds ordered quantity ("
                    + matchingItem.getQuantityOrdered() + ") for " + medicine.getMedicineName());
        }

        matchingItem.setQuantityReceived(newTotalReceived);
        purchaseOrderItemRepository.save(matchingItem);

        // Insert BatchInventory
        BatchInventory batch = new BatchInventory();
        batch.setMedicine(medicine);
        batch.setBatchNumber(request.getBatchNumber().trim());
        batch.setExpiryDate(request.getExpiryDate());
        batch.setMfgDate(request.getMfgDate());
        batch.setQuantityInStock(request.getQuantityReceived());
        batch.setPurchaseUnitCost(request.getPurchaseUnitCost());

        if (request.getLocationId() != null) {
            ShelfLocation loc = shelfLocationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new PharmacyBusinessException("E11", "Shelf Location not found with ID: " + request.getLocationId()));
            batch.setLocation(loc);
        }

        batchInventoryRepository.save(batch);

        // Update PO status
        boolean allReceived = items.stream().allMatch(i -> {
            int recv = i.getMedicine().getMedicineId().equals(request.getMedicineId())
                    ? matchingItem.getQuantityReceived()
                    : (i.getQuantityReceived() == null ? 0 : i.getQuantityReceived());
            return recv >= i.getQuantityOrdered();
        });

        po.setOrderStatus(allReceived ? "COMPLETED" : "PARTIALLY_RECEIVED");
        purchaseOrderRepository.save(po);

        return buildResponseDTO(po);
    }

    // --- BULK RECEIVE STOCK ---
    @Transactional(rollbackFor = Exception.class)
    public PurchaseOrderResponseDTO receiveShipmentBulk(BulkStockIntakeRequestDTO request) {
        PurchaseOrder po = purchaseOrderRepository.findByIdWithDetails(request.getPoId())
                .orElseThrow(() -> new PharmacyBusinessException("E29", "Purchase Order #" + request.getPoId() + " not found."));

        if ("CANCELLED".equalsIgnoreCase(po.getOrderStatus())) {
            throw new PharmacyBusinessException("E29", "Cannot receive stock against a CANCELLED purchase order.");
        }
        if ("COMPLETED".equalsIgnoreCase(po.getOrderStatus()) || "RECEIVED".equalsIgnoreCase(po.getOrderStatus())) {
            throw new PharmacyBusinessException("E29", "This purchase order has already been fully completed.");
        }

        List<PurchaseOrderItem> poItems = purchaseOrderItemRepository.findByPurchaseOrderId(po.getPoId());

        for (BulkStockIntakeRequestDTO.BulkItemDTO itemDTO : request.getItems()) {
            if (itemDTO.getQuantityReceived() == null || itemDTO.getQuantityReceived() <= 0) continue;
            if (itemDTO.getBatchNumber() == null || itemDTO.getBatchNumber().isBlank()) continue;
            if (itemDTO.getExpiryDate() == null) continue;

            // E08: Expiry in future
            if (itemDTO.getExpiryDate().isBefore(LocalDate.now())) {
                throw new PharmacyBusinessException("E08", "Expiry date cannot be in the past: " + itemDTO.getExpiryDate());
            }
            // E09: Mfg < Expiry
            if (itemDTO.getMfgDate() != null && itemDTO.getMfgDate().isAfter(itemDTO.getExpiryDate())) {
                throw new PharmacyBusinessException("E09", "Manufacturing date cannot be after expiry date.");
            }

            Medicine medicine = medicineRepository.findById(itemDTO.getMedicineId())
                    .orElseThrow(() -> new PharmacyBusinessException("E01", "Medicine not found with ID: " + itemDTO.getMedicineId()));

            PurchaseOrderItem matchingItem = poItems.stream()
                    .filter(i -> i.getMedicine().getMedicineId().equals(itemDTO.getMedicineId()))
                    .findFirst()
                    .orElseThrow(() -> new PharmacyBusinessException("E25",
                            "Medicine ID " + itemDTO.getMedicineId() + " not found in PO #" + po.getPoId()));

            int previousReceived = matchingItem.getQuantityReceived() == null ? 0 : matchingItem.getQuantityReceived();
            int newTotal = previousReceived + itemDTO.getQuantityReceived();

            // E30: Over-delivery check
            if (newTotal > matchingItem.getQuantityOrdered()) {
                throw new PharmacyBusinessException("E30",
                        "Received quantity exceeds ordered quantity for " + medicine.getMedicineName());
            }

            matchingItem.setQuantityReceived(newTotal);
            purchaseOrderItemRepository.save(matchingItem);

            BatchInventory batch = new BatchInventory();
            batch.setMedicine(medicine);
            batch.setBatchNumber(itemDTO.getBatchNumber().trim());
            batch.setExpiryDate(itemDTO.getExpiryDate());
            batch.setMfgDate(itemDTO.getMfgDate());
            batch.setQuantityInStock(itemDTO.getQuantityReceived());
            batch.setPurchaseUnitCost(itemDTO.getPurchaseUnitCost());

            if (itemDTO.getLocationId() != null) {
                ShelfLocation loc = shelfLocationRepository.findById(itemDTO.getLocationId())
                        .orElseThrow(() -> new PharmacyBusinessException("E11", "Shelf Location not found with ID: " + itemDTO.getLocationId()));
                batch.setLocation(loc);
            }

            batchInventoryRepository.save(batch);
        }

        // Evaluate overall PO completion
        boolean allReceived = poItems.stream().allMatch(i -> {
            int recv = i.getQuantityReceived() == null ? 0 : i.getQuantityReceived();
            return recv >= i.getQuantityOrdered();
        });

        po.setOrderStatus(allReceived ? "COMPLETED" : "PARTIALLY_RECEIVED");
        purchaseOrderRepository.save(po);

        return buildResponseDTO(po);
    }

    // --- CANCEL PURCHASE ORDER ---
    @Transactional(rollbackFor = Exception.class)
    public PurchaseOrderResponseDTO cancelPurchaseOrder(Long poId) {
        PurchaseOrder po = purchaseOrderRepository.findByIdWithDetails(poId)
                .orElseThrow(() -> new PharmacyBusinessException("E29", "Purchase Order #" + poId + " not found."));

        if (!"PENDING".equalsIgnoreCase(po.getOrderStatus())) {
            throw new PharmacyBusinessException("E29",
                    "Cannot cancel Purchase Order #" + poId + " because its status is '" + po.getOrderStatus() + "'.");
        }

        po.setOrderStatus("CANCELLED");
        po = purchaseOrderRepository.save(po);
        return buildResponseDTO(po);
    }

    // --- GET RECEIVED INVENTORY LOG ---
    @Transactional(readOnly = true)
    public List<BatchInventoryDTO> getReceivedInventoryLog() {
        return batchInventoryRepository.findAllWithDetails().stream().map(batch -> {
            BatchInventoryDTO dto = new BatchInventoryDTO();
            dto.setBatchId(batch.getBatchId());
            dto.setBatchNumber(batch.getBatchNumber());
            dto.setQuantityInStock(batch.getQuantityInStock());
            dto.setPurchaseUnitCost(batch.getPurchaseUnitCost());
            dto.setExpiryDate(batch.getExpiryDate());
            dto.setMfgDate(batch.getMfgDate());
            if (batch.getMedicine() != null) {
                dto.setMedicineId(batch.getMedicine().getMedicineId());
                dto.setMedicineName(batch.getMedicine().getMedicineName());
                dto.setGenericName(batch.getMedicine().getGenericName());
                dto.setDosageForm(batch.getMedicine().getDosageForm());
                dto.setScheduleType(batch.getMedicine().getScheduleType());
            }
            if (batch.getLocation() != null) {
                ShelfLocation loc = batch.getLocation();
                dto.setLocationId(loc.getLocationId());
                dto.setRackCode(loc.getRackCode());
                dto.setAisleNumber(loc.getAisleNumber() != null ? String.valueOf(loc.getAisleNumber()) : null);
                dto.setShelfNumber(loc.getShelfNumber() != null ? String.valueOf(loc.getShelfNumber()) : null);
                dto.setTemperatureZone(loc.getTemperatureZone());
                dto.setShelfLocationDisplay(MedicineService.formatShelfLocation(loc));
            } else {
                dto.setShelfLocationDisplay("Unassigned");
            }
            return dto;
        }).collect(Collectors.toList());
    }

    private PurchaseOrderResponseDTO buildResponseDTO(PurchaseOrder po) {
        List<PurchaseOrderItem> items = purchaseOrderItemRepository.findByPurchaseOrderId(po.getPoId());

        List<PurchaseOrderResponseDTO.ItemDTO> itemDTOs = items.stream().map(i ->
                new PurchaseOrderResponseDTO.ItemDTO(
                        i.getMedicine().getMedicineId(),
                        i.getMedicine().getMedicineName(),
                        i.getQuantityOrdered(),
                        i.getQuantityReceived() == null ? 0 : i.getQuantityReceived(),
                        i.getPurchasePrice()
                )
        ).collect(Collectors.toList());

        Long empId = po.getCreatedByEmployee() != null ? po.getCreatedByEmployee().getEmployeeId() : null;
        String empName = po.getCreatedByEmployee() != null
                ? (po.getCreatedByEmployee().getFirstName() + " " + (po.getCreatedByEmployee().getLastName() != null ? po.getCreatedByEmployee().getLastName() : "")).trim()
                : "System";

        return new PurchaseOrderResponseDTO(
                po.getPoId(),
                po.getSupplier() != null ? po.getSupplier().getSupplierId() : null,
                po.getSupplier() != null ? po.getSupplier().getSupplierName() : "Unknown",
                empId,
                empName,
                po.getOrderDate(),
                po.getOrderStatus(),
                po.getExpectedDeliveryDate(),
                itemDTOs
        );
    }
}
