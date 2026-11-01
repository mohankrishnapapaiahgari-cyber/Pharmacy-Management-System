package com.example.demo.service;

import com.example.demo.dto.MedicineBatchDTO;
import com.example.demo.dto.MedicineSearchDTO;
import com.example.demo.model.BatchInventory;
import com.example.demo.model.Medicine;
import com.example.demo.model.ShelfLocation;
import com.example.demo.repository.BatchInventoryRepository;
import com.example.demo.repository.MedicineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final BatchInventoryRepository batchInventoryRepository;

    public MedicineService(MedicineRepository medicineRepository,
                           BatchInventoryRepository batchInventoryRepository) {
        this.medicineRepository = medicineRepository;
        this.batchInventoryRepository = batchInventoryRepository;
    }

    /**
     * Formats shelf location into a friendly human-readable display string:
     * "Aisle: {aisle}, Rack: {rack}, Shelf: {shelf}" (e.g. "Aisle: 1, Rack: B, Shelf: 2")
     */
    public static String formatShelfLocation(ShelfLocation loc) {
        if (loc == null) return "Unassigned";
        return formatShelfLocation(loc.getAisleNumber(), loc.getRackCode(), loc.getShelfNumber());
    }

    public static String formatShelfLocation(Integer aisle, String rack, Integer shelf) {
        if (aisle == null && (rack == null || rack.isBlank()) && shelf == null) {
            return "Unassigned";
        }
        List<String> parts = new ArrayList<>();
        if (aisle != null) {
            parts.add("Aisle: " + aisle);
        }
        if (rack != null && !rack.isBlank()) {
            parts.add("Rack: " + rack.trim());
        }
        if (shelf != null) {
            parts.add("Shelf: " + shelf);
        }
        return parts.isEmpty() ? "Unassigned" : String.join(", ", parts);
    }

    /**
     * Maps BatchInventory entities to MedicineBatchDTOs with FEFO recommendation indicator.
     */
    public List<MedicineBatchDTO> mapBatchesToDTO(List<BatchInventory> batches) {
        List<MedicineBatchDTO> dtoList = new ArrayList<>();
        for (int i = 0; i < batches.size(); i++) {
            BatchInventory b = batches.get(i);
            ShelfLocation loc = b.getLocation();
            String locDisplay = formatShelfLocation(loc);

            dtoList.add(new MedicineBatchDTO(
                    b.getBatchId(),
                    b.getMedicine() != null ? b.getMedicine().getMedicineId() : null,
                    b.getMedicine() != null ? b.getMedicine().getMedicineName() : "",
                    b.getBatchNumber(),
                    b.getExpiryDate(),
                    b.getMfgDate(),
                    b.getQuantityInStock(),
                    b.getPurchaseUnitCost(),
                    b.getMedicine() != null ? b.getMedicine().getUnitPrice() : null,
                    loc != null ? loc.getLocationId() : null,
                    loc != null ? loc.getAisleNumber() : null,
                    loc != null ? loc.getRackCode() : null,
                    loc != null ? loc.getShelfNumber() : null,
                    loc != null ? loc.getTemperatureZone() : null,
                    locDisplay,
                    i == 0 // Earliest expiry batch is recommended FEFO
            ));
        }
        return dtoList;
    }

    /**
     * Returns all active batches for a given medicine with location details in FEFO order.
     */
    @Transactional(readOnly = true)
    public List<MedicineBatchDTO> getMedicineBatches(Long medicineId) {
        List<BatchInventory> batches = batchInventoryRepository.findActiveBatchesByMedicineWithLocation(medicineId);
        return mapBatchesToDTO(batches);
    }

    /**
     * Searches medicines by name or generic name (partial, case-insensitive).
     * Calculates total available stock dynamically across all non-expired batches
     * and attaches physical locations and batch breakdown.
     */
    @Transactional(readOnly = true)
    public List<MedicineSearchDTO> searchMedicines(String keyword) {
        List<Medicine> medicines = medicineRepository.searchAvailableMedicines(keyword);

        return medicines.stream().map(m -> {
            List<BatchInventory> batches = batchInventoryRepository.findActiveBatchesByMedicineWithLocation(m.getMedicineId());
            List<MedicineBatchDTO> batchDTOs = mapBatchesToDTO(batches);
            int totalStock = batches.stream().mapToInt(BatchInventory::getQuantityInStock).sum();

            String primaryLoc = batchDTOs.stream()
                    .map(MedicineBatchDTO::getShelfLocationDisplay)
                    .filter(l -> l != null && !l.equals("Unassigned"))
                    .findFirst()
                    .orElse("Unassigned");

            MedicineSearchDTO dto = new MedicineSearchDTO(
                    m.getMedicineId(),
                    m.getMedicineName(),
                    m.getGenericName(),
                    m.getDosageForm() != null ? m.getDosageForm() : "Tablet",
                    m.getStrength() != null ? m.getStrength() : "",
                    m.getUnitPrice(),
                    totalStock,
                    m.getScheduleType()
            );
            dto.setPrimaryShelfLocation(primaryLoc);
            dto.setBatches(batchDTOs);
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Retrieves a single medicine with its current aggregated available stock and batches.
     */
    @Transactional(readOnly = true)
    public MedicineSearchDTO getMedicineById(Long medicineId) {
        Medicine m = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new RuntimeException("Medicine not found with ID: " + medicineId));
        List<BatchInventory> batches = batchInventoryRepository.findActiveBatchesByMedicineWithLocation(m.getMedicineId());
        List<MedicineBatchDTO> batchDTOs = mapBatchesToDTO(batches);
        int totalStock = batches.stream().mapToInt(BatchInventory::getQuantityInStock).sum();

        String primaryLoc = batchDTOs.stream()
                .map(MedicineBatchDTO::getShelfLocationDisplay)
                .filter(l -> l != null && !l.equals("Unassigned"))
                .findFirst()
                .orElse("Unassigned");

        MedicineSearchDTO dto = new MedicineSearchDTO(
                m.getMedicineId(),
                m.getMedicineName(),
                m.getGenericName(),
                m.getDosageForm() != null ? m.getDosageForm() : "Tablet",
                m.getStrength() != null ? m.getStrength() : "",
                m.getUnitPrice(),
                totalStock,
                m.getScheduleType()
        );
        dto.setPrimaryShelfLocation(primaryLoc);
        dto.setBatches(batchDTOs);
        return dto;
    }
}
