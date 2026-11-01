package com.example.demo.repository;

import com.example.demo.model.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {

    /**
     * Financial aggregation: returns [totalRevenue, totalTax, transactionCount, totalItemsSold]
     * as a single-row Object array. All values computed dynamically from InvoiceItem records.
     */
    @Query("""
           SELECT COALESCE(SUM(ii.unitSellingPrice * ii.quantity), 0),
                  COALESCE(SUM(ii.unitSellingPrice * ii.quantity * ii.taxRate / 100), 0),
                  COUNT(DISTINCT ii.invoice),
                  COALESCE(SUM(ii.quantity), 0)
           FROM InvoiceItem ii
           """)
    List<Object[]> getFinancialSummary();

    /**
     * Returns all invoice items for a given invoice, with batch and medicine info eagerly loaded.
     */
    @Query("""
           SELECT ii FROM InvoiceItem ii
           JOIN FETCH ii.batch b
           JOIN FETCH b.medicine
           WHERE ii.invoice.invoiceId = :invoiceId
           """)
    List<InvoiceItem> findByInvoiceIdWithDetails(Long invoiceId);
}
