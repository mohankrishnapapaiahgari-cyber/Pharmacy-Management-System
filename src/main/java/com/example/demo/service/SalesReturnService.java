package com.example.demo.service;

import com.example.demo.dto.InvoiceLookupDTO;
import com.example.demo.dto.SalesReturnRequestDTO;
import com.example.demo.dto.SalesReturnResponseDTO;
import com.example.demo.exception.PharmacyBusinessException;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SalesReturnService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final SalesReturnRepository salesReturnRepository;
    private final SalesReturnItemRepository salesReturnItemRepository;
    private final BatchInventoryRepository batchInventoryRepository;
    private final EmployeeRepository employeeRepository;
    private final PaymentRepository paymentRepository;

    public SalesReturnService(InvoiceRepository invoiceRepository,
                              InvoiceItemRepository invoiceItemRepository,
                              SalesReturnRepository salesReturnRepository,
                              SalesReturnItemRepository salesReturnItemRepository,
                              BatchInventoryRepository batchInventoryRepository,
                              EmployeeRepository employeeRepository,
                              PaymentRepository paymentRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.salesReturnRepository = salesReturnRepository;
        this.salesReturnItemRepository = salesReturnItemRepository;
        this.batchInventoryRepository = batchInventoryRepository;
        this.employeeRepository = employeeRepository;
        this.paymentRepository = paymentRepository;
    }

    /**
     * Looks up an invoice for the returns workflow.
     * Computes maximum returnable quantities per invoice item.
     */
    @Transactional(readOnly = true)
    public InvoiceLookupDTO findInvoiceForReturn(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new PharmacyBusinessException("E52", "Invoice #" + invoiceId + " does not exist."));

        List<InvoiceItem> invoiceItems = invoiceItemRepository.findByInvoiceIdWithDetails(invoiceId);
        if (invoiceItems.isEmpty()) {
            throw new PharmacyBusinessException("E52", "Invoice #" + invoiceId + " has no line items.");
        }

        // Calculate already returned quantities per invoice_item_id
        Map<Long, Integer> alreadyReturnedMap = new HashMap<>();
        List<SalesReturn> pastReturns = salesReturnRepository.findAllByOrderByReturnDateDesc();
        for (SalesReturn sr : pastReturns) {
            if (sr.getInvoice().getInvoiceId().equals(invoiceId)) {
                List<SalesReturnItem> returnItems = salesReturnItemRepository.findBySalesReturnReturnId(sr.getReturnId());
                for (SalesReturnItem ri : returnItems) {
                    Long itemId = ri.getInvoiceItem().getInvoiceItemId();
                    alreadyReturnedMap.merge(itemId, ri.getQuantityReturned(), Integer::sum);
                }
            }
        }

        List<InvoiceLookupDTO.InvoiceBatchItem> items = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (InvoiceItem ii : invoiceItems) {
            BatchInventory batch = ii.getBatch();
            Medicine med = batch.getMedicine();

            BigDecimal lineSubtotal = ii.getUnitSellingPrice().multiply(BigDecimal.valueOf(ii.getQuantity()));
            BigDecimal lineTax = lineSubtotal.multiply(ii.getTaxRate()).divide(HUNDRED, 2, RoundingMode.HALF_UP);
            grandTotal = grandTotal.add(lineSubtotal).add(lineTax);

            int alreadyReturned = alreadyReturnedMap.getOrDefault(ii.getInvoiceItemId(), 0);
            int maxReturnable = ii.getQuantity() - alreadyReturned;

            InvoiceLookupDTO.InvoiceBatchItem item = new InvoiceLookupDTO.InvoiceBatchItem();
            item.setInvoiceItemId(ii.getInvoiceItemId());
            item.setBatchId(batch.getBatchId());
            item.setBatchNumber(batch.getBatchNumber());
            item.setMedicineId(med.getMedicineId());
            item.setMedicineName(med.getMedicineName());
            item.setQuantitySold(ii.getQuantity());
            item.setQuantityAlreadyReturned(alreadyReturned);
            item.setMaxReturnable(Math.max(0, maxReturnable));
            item.setUnitSellingPrice(ii.getUnitSellingPrice());
            item.setTaxRate(ii.getTaxRate());
            items.add(item);
        }

        // Determine payment method
        String paymentMethod = "CASH";
        List<Payment> payments = paymentRepository.findByInvoiceInvoiceId(invoiceId);
        if (!payments.isEmpty()) {
            paymentMethod = payments.get(0).getPaymentMethod();
        }

        InvoiceLookupDTO dto = new InvoiceLookupDTO();
        dto.setInvoiceId(invoice.getInvoiceId());
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setCustomerName(invoice.getCustomer() != null ? invoice.getCustomer().getFullName() : "Walk-in Customer");
        dto.setPaymentStatus(invoice.getPaymentStatus());
        dto.setPaymentMethod(paymentMethod);
        dto.setItems(items);
        dto.setGrandTotal(grandTotal.subtract(invoice.getDiscountAmount()));
        return dto;
    }

    /**
     * Processes sales return atomically (E52 - E59):
     * 1. Validates invoice and employee.
     * 2. Validates item eligibility and return limits.
     * 3. Creates SALES_RETURN and SALES_RETURN_ITEM.
     * 4. If RETURNED_TO_STOCK and not expired, increments batch quantity.
     * 5. Rolls back on failure.
     */
    @Transactional(rollbackFor = Exception.class)
    public SalesReturnResponseDTO processReturn(SalesReturnRequestDTO request) {
        // E52: Invalid invoice
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new PharmacyBusinessException("E52", "Invoice #" + request.getInvoiceId() + " does not exist."));

        // E40: Resolve Employee
        Long empId = request.getEmployeeId() != null ? request.getEmployeeId() : 1L;
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new PharmacyBusinessException("E40", "Employee not found with ID: " + empId));

        // E57: Empty return items
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new PharmacyBusinessException("E57", "No return items specified.");
        }

        // Load invoice items
        List<InvoiceItem> invoiceItems = invoiceItemRepository.findByInvoiceIdWithDetails(request.getInvoiceId());
        Map<Long, InvoiceItem> byItemId = new HashMap<>();
        Map<Long, InvoiceItem> byBatchId = new HashMap<>();
        for (InvoiceItem ii : invoiceItems) {
            byItemId.put(ii.getInvoiceItemId(), ii);
            byBatchId.put(ii.getBatch().getBatchId(), ii);
        }

        // Calculate already returned amounts
        Map<Long, Integer> alreadyReturnedMap = new HashMap<>();
        List<SalesReturn> pastReturns = salesReturnRepository.findAllByOrderByReturnDateDesc();
        for (SalesReturn sr : pastReturns) {
            if (sr.getInvoice().getInvoiceId().equals(request.getInvoiceId())) {
                List<SalesReturnItem> returnItems = salesReturnItemRepository.findBySalesReturnReturnId(sr.getReturnId());
                for (SalesReturnItem ri : returnItems) {
                    alreadyReturnedMap.merge(ri.getInvoiceItem().getInvoiceItemId(), ri.getQuantityReturned(), Integer::sum);
                }
            }
        }

        // Create SALES_RETURN header
        SalesReturn salesReturn = new SalesReturn();
        salesReturn.setInvoice(invoice);
        salesReturn.setProcessedByEmployee(employee);
        salesReturn.setReturnDate(LocalDateTime.now());
        salesReturn.setReason(request.getReason() != null ? request.getReason() : "Customer Return");
        salesReturn = salesReturnRepository.save(salesReturn);

        List<SalesReturnResponseDTO.ReturnItemDetail> returnDetails = new ArrayList<>();
        BigDecimal totalRefund = BigDecimal.ZERO;

        for (SalesReturnRequestDTO.ReturnItemDTO returnItem : request.getItems()) {
            if (returnItem.getQuantityReturned() == null || returnItem.getQuantityReturned() <= 0) {
                continue; // Skip 0 quantities
            }

            // E56: Negative return qty
            if (returnItem.getQuantityReturned() < 0) {
                throw new PharmacyBusinessException("E56", "Return quantity cannot be negative.");
            }

            // Find matching invoice item
            InvoiceItem originalItem = null;
            if (returnItem.getInvoiceItemId() != null) {
                originalItem = byItemId.get(returnItem.getInvoiceItemId());
            } else if (returnItem.getBatchId() != null) {
                originalItem = byBatchId.get(returnItem.getBatchId());
            }

            // E53: Item doesn't belong to invoice
            if (originalItem == null) {
                throw new PharmacyBusinessException("E53", "Selected item was not billed on Invoice #" + request.getInvoiceId());
            }

            // E54 / E55: Return qty validation against sold and already returned
            int alreadyReturned = alreadyReturnedMap.getOrDefault(originalItem.getInvoiceItemId(), 0);
            int maxReturnable = originalItem.getQuantity() - alreadyReturned;

            if (returnItem.getQuantityReturned() > originalItem.getQuantity()) {
                throw new PharmacyBusinessException("E54", "Return quantity (" + returnItem.getQuantityReturned()
                        + ") exceeds original sold quantity (" + originalItem.getQuantity() + ").");
            }

            if (returnItem.getQuantityReturned() > maxReturnable) {
                throw new PharmacyBusinessException("E55", "Return quantity (" + returnItem.getQuantityReturned()
                        + ") exceeds max returnable quantity (" + maxReturnable + ") for "
                        + originalItem.getBatch().getMedicine().getMedicineName());
            }

            // Calculate refund price per unit
            BigDecimal unitPrice = originalItem.getUnitSellingPrice();
            BigDecimal taxPerUnit = unitPrice.multiply(originalItem.getTaxRate()).divide(HUNDRED, 2, RoundingMode.HALF_UP);
            BigDecimal refundPricePerUnit = unitPrice.add(taxPerUnit);

            // Determine disposition: RETURNED_TO_STOCK vs DISCARDED
            String disp = returnItem.getDisposition();
            if (disp == null || disp.isBlank() || disp.equalsIgnoreCase("RESTOCK")) {
                disp = "RETURNED_TO_STOCK";
            } else if (disp.equalsIgnoreCase("DAMAGED_DISPOSAL") || disp.equalsIgnoreCase("DISCARDED")) {
                disp = "DISCARDED";
            }

            BatchInventory batch = originalItem.getBatch();

            // E58: Expired batch check for restocking
            if ("RETURNED_TO_STOCK".equalsIgnoreCase(disp)) {
                if (batch.getExpiryDate() != null && batch.getExpiryDate().isBefore(LocalDate.now())) {
                    disp = "DISCARDED"; // Automatically forces DISCARDED to prevent dispensing expired stock
                } else {
                    // Restock inventory
                    batch.setQuantityInStock(batch.getQuantityInStock() + returnItem.getQuantityReturned());
                    batchInventoryRepository.save(batch);
                }
            }

            // Save SALES_RETURN_ITEM
            SalesReturnItem sri = new SalesReturnItem();
            sri.setSalesReturn(salesReturn);
            sri.setInvoiceItem(originalItem);
            sri.setQuantityReturned(returnItem.getQuantityReturned());
            sri.setRefundPrice(refundPricePerUnit);
            sri.setDisposition(disp);
            salesReturnItemRepository.save(sri);

            BigDecimal lineRefund = refundPricePerUnit.multiply(BigDecimal.valueOf(returnItem.getQuantityReturned()));
            totalRefund = totalRefund.add(lineRefund);

            SalesReturnResponseDTO.ReturnItemDetail detail = new SalesReturnResponseDTO.ReturnItemDetail();
            detail.setBatchId(batch.getBatchId());
            detail.setBatchNumber(batch.getBatchNumber());
            detail.setMedicineName(batch.getMedicine().getMedicineName());
            detail.setQuantityReturned(returnItem.getQuantityReturned());
            detail.setRefundPrice(refundPricePerUnit);
            detail.setLineRefund(lineRefund);
            detail.setDisposition(disp);
            returnDetails.add(detail);
        }

        if (returnDetails.isEmpty()) {
            throw new PharmacyBusinessException("E57", "No valid items selected for return.");
        }

        SalesReturnResponseDTO response = new SalesReturnResponseDTO();
        response.setReturnId(salesReturn.getReturnId());
        response.setInvoiceId(invoice.getInvoiceId());
        response.setProcessedByName(employee.getFirstName() + " " + employee.getLastName());
        response.setReturnDate(salesReturn.getReturnDate());
        response.setReason(salesReturn.getReason());
        response.setItems(returnDetails);
        response.setTotalRefund(totalRefund);

        return response;
    }

    /**
     * Lists audit history of all returns.
     */
    @Transactional(readOnly = true)
    public List<SalesReturnResponseDTO> getReturnHistory() {
        List<SalesReturn> returns = salesReturnRepository.findAllByOrderByReturnDateDesc();
        List<SalesReturnResponseDTO> result = new ArrayList<>();

        for (SalesReturn sr : returns) {
            List<SalesReturnItem> items = salesReturnItemRepository.findBySalesReturnReturnId(sr.getReturnId());
            List<SalesReturnResponseDTO.ReturnItemDetail> details = new ArrayList<>();
            BigDecimal totalRefund = BigDecimal.ZERO;

            for (SalesReturnItem ri : items) {
                BigDecimal lineRefund = ri.getRefundPrice().multiply(BigDecimal.valueOf(ri.getQuantityReturned()));
                totalRefund = totalRefund.add(lineRefund);

                BatchInventory b = ri.getInvoiceItem().getBatch();
                SalesReturnResponseDTO.ReturnItemDetail detail = new SalesReturnResponseDTO.ReturnItemDetail();
                detail.setBatchId(b.getBatchId());
                detail.setBatchNumber(b.getBatchNumber());
                detail.setMedicineName(b.getMedicine().getMedicineName());
                detail.setQuantityReturned(ri.getQuantityReturned());
                detail.setRefundPrice(ri.getRefundPrice());
                detail.setLineRefund(lineRefund);
                detail.setDisposition(ri.getDisposition());
                details.add(detail);
            }

            SalesReturnResponseDTO dto = new SalesReturnResponseDTO();
            dto.setReturnId(sr.getReturnId());
            dto.setInvoiceId(sr.getInvoice().getInvoiceId());
            dto.setProcessedByName(sr.getProcessedByEmployee().getFirstName() + " " + sr.getProcessedByEmployee().getLastName());
            dto.setReturnDate(sr.getReturnDate());
            dto.setReason(sr.getReason());
            dto.setItems(details);
            dto.setTotalRefund(totalRefund);
            result.add(dto);
        }

        return result;
    }
}
