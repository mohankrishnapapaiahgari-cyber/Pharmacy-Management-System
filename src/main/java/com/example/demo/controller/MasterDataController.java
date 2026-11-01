package com.example.demo.controller;

import com.example.demo.exception.PharmacyBusinessException;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/master")
public class MasterDataController {

    private final CategoryRepository categoryRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final ShelfLocationRepository shelfLocationRepository;
    private final MedicineRepository medicineRepository;
    private final BatchInventoryRepository batchInventoryRepository;

    public MasterDataController(CategoryRepository categoryRepository,
                                ManufacturerRepository manufacturerRepository,
                                ShelfLocationRepository shelfLocationRepository,
                                MedicineRepository medicineRepository,
                                BatchInventoryRepository batchInventoryRepository) {
        this.categoryRepository = categoryRepository;
        this.manufacturerRepository = manufacturerRepository;
        this.shelfLocationRepository = shelfLocationRepository;
        this.medicineRepository = medicineRepository;
        this.batchInventoryRepository = batchInventoryRepository;
    }

    // =========================================================
    // 1. CATEGORIES
    // =========================================================
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> listCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        if (category.getCategoryName() == null || category.getCategoryName().isBlank()) {
            throw new PharmacyBusinessException("E22", "Category name is required.");
        }
        boolean exists = categoryRepository.findAll().stream()
                .anyMatch(c -> category.getCategoryName().trim().equalsIgnoreCase(c.getCategoryName()));
        if (exists) {
            throw new PharmacyBusinessException("E01", "Category with name " + category.getCategoryName() + " already exists.");
        }
        Category saved = categoryRepository.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // =========================================================
    // 2. MANUFACTURERS
    // =========================================================
    @GetMapping("/manufacturers")
    public ResponseEntity<List<Manufacturer>> listManufacturers() {
        return ResponseEntity.ok(manufacturerRepository.findAll());
    }

    @PostMapping("/manufacturers")
    public ResponseEntity<?> createManufacturer(@RequestBody Manufacturer manufacturer) {
        if (manufacturer.getManufacturerName() == null || manufacturer.getManufacturerName().isBlank()) {
            throw new PharmacyBusinessException("E22", "Manufacturer name is required.");
        }

        // E17: Duplicate manufacturer license
        if (manufacturer.getLicenseNumber() != null && !manufacturer.getLicenseNumber().isBlank()) {
            boolean exists = manufacturerRepository.findAll().stream()
                    .anyMatch(m -> manufacturer.getLicenseNumber().equalsIgnoreCase(m.getLicenseNumber()));
            if (exists) {
                throw new PharmacyBusinessException("E17", "Manufacturer with license number " + manufacturer.getLicenseNumber() + " already exists.");
            }
        }

        Manufacturer saved = manufacturerRepository.save(manufacturer);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // =========================================================
    // 3. SHELF LOCATIONS
    // =========================================================
    @GetMapping("/locations")
    public ResponseEntity<List<ShelfLocation>> listLocations() {
        return ResponseEntity.ok(shelfLocationRepository.findAll());
    }

    @PostMapping("/locations")
    public ResponseEntity<?> createLocation(@RequestBody ShelfLocation location) {
        if (location.getRackCode() == null || location.getRackCode().isBlank()) {
            throw new PharmacyBusinessException("E22", "Rack code is required.");
        }
        ShelfLocation saved = shelfLocationRepository.save(location);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // =========================================================
    // 4. MEDICINES CRUD
    // =========================================================
    @GetMapping("/medicines")
    public ResponseEntity<List<Map<String, Object>>> listMedicines() {
        List<Medicine> medicines = medicineRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Medicine m : medicines) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("medicineId", m.getMedicineId());
            map.put("medicineName", m.getMedicineName());
            map.put("genericName", m.getGenericName());
            map.put("brandName", m.getBrandName());
            map.put("dosageForm", m.getDosageForm());
            map.put("strength", m.getStrength());
            map.put("scheduleType", m.getScheduleType());
            map.put("unitPrice", m.getUnitPrice());
            map.put("reorderLevel", m.getReorderLevel());
            map.put("reorderQuantity", m.getReorderQuantity());
            map.put("isActive", m.getIsActive());
            map.put("category", m.getCategory());
            map.put("manufacturer", m.getManufacturer());

            List<BatchInventory> batches = batchInventoryRepository.findActiveBatchesByMedicineWithLocation(m.getMedicineId());
            List<String> distinctLocs = batches.stream()
                    .map(b -> formatShelfLocation(b.getLocation()))
                    .filter(loc -> !loc.equals("Unassigned"))
                    .distinct()
                    .collect(Collectors.toList());

            String primaryLoc = distinctLocs.isEmpty() ? "Unassigned" : String.join("; ", distinctLocs);
            int totalStock = batches.stream().mapToInt(BatchInventory::getQuantityInStock).sum();

            map.put("primaryShelfLocation", primaryLoc);
            map.put("locations", distinctLocs);
            map.put("activeBatchesCount", batches.size());
            map.put("totalStock", totalStock);

            result.add(map);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/medicines/{id}/batches")
    public ResponseEntity<List<Map<String, Object>>> getMedicineBatches(@PathVariable("id") Long id) {
        List<BatchInventory> batches = batchInventoryRepository.findActiveBatchesByMedicineWithLocation(id);
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < batches.size(); i++) {
            BatchInventory b = batches.get(i);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("batchId", b.getBatchId());
            map.put("batchNumber", b.getBatchNumber());
            map.put("expiryDate", b.getExpiryDate());
            map.put("quantityInStock", b.getQuantityInStock());
            map.put("purchaseUnitCost", b.getPurchaseUnitCost());
            map.put("unitPrice", b.getMedicine() != null ? b.getMedicine().getUnitPrice() : null);
            map.put("shelfLocationDisplay", formatShelfLocation(b.getLocation()));
            map.put("recommendedFefo", i == 0);
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    private String formatShelfLocation(ShelfLocation loc) {
        return com.example.demo.service.MedicineService.formatShelfLocation(loc);
    }

    @PostMapping("/medicines")
    public ResponseEntity<?> createMedicine(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.get("medicineName");
        String genericName = (String) payload.get("genericName");
        String brandName = (String) payload.get("brandName");
        String dosageForm = (String) payload.get("dosageForm");
        String strength = (String) payload.get("strength");
        String scheduleType = (String) payload.get("scheduleType");

        // E22: Missing required name/dosage
        if (name == null || name.isBlank()) {
            throw new PharmacyBusinessException("E22", "Medicine name is required.");
        }

        // E01: Duplicate medicine name
        boolean exists = medicineRepository.findAll().stream()
                .anyMatch(m -> name.trim().equalsIgnoreCase(m.getMedicineName()));
        if (exists) {
            throw new PharmacyBusinessException("E01", "Medicine with name '" + name + "' already exists.");
        }

        // E04: Unit price <= 0
        BigDecimal price = payload.get("unitPrice") != null ? new BigDecimal(payload.get("unitPrice").toString()) : BigDecimal.ZERO;
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PharmacyBusinessException("E04", "Unit price must be greater than zero.");
        }

        // E05: Reorder level <= 0
        int reorderLevel = payload.get("reorderLevel") != null ? Integer.parseInt(payload.get("reorderLevel").toString()) : 10;
        if (reorderLevel <= 0) {
            throw new PharmacyBusinessException("E05", "Reorder level must be a positive integer.");
        }

        // E06: Reorder quantity <= 0
        int reorderQty = payload.get("reorderQuantity") != null ? Integer.parseInt(payload.get("reorderQuantity").toString()) : 50;
        if (reorderQty <= 0) {
            throw new PharmacyBusinessException("E06", "Reorder quantity must be a positive integer.");
        }

        Category cat = null;
        if (payload.get("categoryId") != null) {
            Long catId = Long.parseLong(payload.get("categoryId").toString());
            cat = categoryRepository.findById(catId)
                    .orElseThrow(() -> new PharmacyBusinessException("E02", "Invalid Category ID: " + catId));
        }

        Manufacturer mfg = null;
        if (payload.get("manufacturerId") != null) {
            Long mfgId = Long.parseLong(payload.get("manufacturerId").toString());
            mfg = manufacturerRepository.findById(mfgId)
                    .orElseThrow(() -> new PharmacyBusinessException("E03", "Invalid Manufacturer ID: " + mfgId));
        }

        Medicine med = new Medicine();
        med.setMedicineName(name.trim());
        med.setGenericName(genericName != null ? genericName.trim() : "");
        med.setBrandName(brandName != null ? brandName.trim() : "");
        med.setDosageForm(dosageForm != null && !dosageForm.isBlank() ? dosageForm.trim() : "Tablet");
        med.setStrength(strength != null ? strength.trim() : "");
        med.setScheduleType(scheduleType != null ? scheduleType.trim() : "OTC");
        med.setUnitPrice(price);
        med.setReorderLevel(reorderLevel);
        med.setReorderQuantity(reorderQty);
        med.setIsActive(true);
        med.setCategory(cat);
        med.setManufacturer(mfg);

        Medicine saved = medicineRepository.save(med);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/medicines/{id}/toggle-active")
    public ResponseEntity<?> toggleMedicineActive(@PathVariable("id") Long id) {
        Medicine med = medicineRepository.findById(id)
                .orElseThrow(() -> new PharmacyBusinessException("E01", "Medicine not found with ID: " + id));

        // E07: Warning / alert if deactivating medicine with active stock
        if (med.getIsActive() != null && med.getIsActive()) {
            int activeStock = batchInventoryRepository.findBatchesByMedicineFEFO(id).stream()
                    .mapToInt(BatchInventory::getQuantityInStock).sum();
            if (activeStock > 0) {
                // We deactivate it but report warning note in response
                med.setIsActive(false);
                Medicine saved = medicineRepository.save(med);
                return ResponseEntity.ok(Map.of(
                        "medicine", saved,
                        "warning", "[E07] Warning: Medicine was deactivated while " + activeStock + " units remain in inventory."
                ));
            }
            med.setIsActive(false);
        } else {
            med.setIsActive(true);
        }

        Medicine saved = medicineRepository.save(med);
        return ResponseEntity.ok(Map.of("medicine", saved));
    }
}
