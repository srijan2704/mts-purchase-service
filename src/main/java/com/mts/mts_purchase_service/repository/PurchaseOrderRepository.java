package com.mts.mts_purchase_service.repository;

import com.mts.mts_purchase_service.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for purchase order headers and detail fetch operations.
 */
@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    /** Returns orders by date sorted by newest first. */
    @EntityGraph(attributePaths = {"seller", "items", "items.variant", "items.variant.product", "items.variant.unit"})
    List<PurchaseOrder> findByOrderDateOrderByCreatedAtDesc(LocalDate orderDate);

    /** Returns orders by date and seller sorted by newest first. */
    @EntityGraph(attributePaths = {"seller", "items", "items.variant", "items.variant.product", "items.variant.unit"})
    List<PurchaseOrder> findByOrderDateAndSellerSellerIdOrderByCreatedAtDesc(LocalDate orderDate, Long sellerId);

    /** Returns orders in date range sorted by order date desc, then newest first. */
    @EntityGraph(attributePaths = {"seller", "items", "items.variant", "items.variant.product", "items.variant.unit"})
    List<PurchaseOrder> findByOrderDateBetweenOrderByOrderDateDescCreatedAtDesc(LocalDate fromDate, LocalDate toDate);

    /** Returns seller orders in date range sorted by order date desc, then newest first. */
    @EntityGraph(attributePaths = {"seller", "items", "items.variant", "items.variant.product", "items.variant.unit"})
    List<PurchaseOrder> findByOrderDateBetweenAndSellerSellerIdOrderByOrderDateDescCreatedAtDesc(
            LocalDate fromDate,
            LocalDate toDate,
            Long sellerId
    );

    /** Returns CONFIRMED orders by date sorted by newest first. */
    @EntityGraph(attributePaths = {"seller", "items", "items.variant", "items.variant.product", "items.variant.unit"})
    List<PurchaseOrder> findByOrderDateAndStatusOrderByCreatedAtDesc(LocalDate orderDate, String status);

    /** Returns orders for one seller in date range sorted by date desc. */
    @EntityGraph(attributePaths = {"seller", "items", "items.variant", "items.variant.product", "items.variant.unit"})
    List<PurchaseOrder> findBySellerSellerIdAndOrderDateBetweenOrderByOrderDateDescCreatedAtDesc(
            Long sellerId,
            LocalDate from,
            LocalDate to
    );

    /** Returns CONFIRMED orders for one seller in date range sorted by date desc. */
    @EntityGraph(attributePaths = {"seller", "items", "items.variant", "items.variant.product", "items.variant.unit"})
    List<PurchaseOrder> findBySellerSellerIdAndOrderDateBetweenAndStatusOrderByOrderDateDescCreatedAtDesc(
            Long sellerId,
            LocalDate from,
            LocalDate to,
            String status
    );

    /** Returns one fully loaded order by id for detail endpoint. */
    @EntityGraph(attributePaths = {"seller", "items", "items.variant", "items.variant.product", "items.variant.unit"})
    Optional<PurchaseOrder> findWithDetailsByOrderId(Long orderId);

    /** Aggregates total purchase amount by date for range trend reports. */
    @Query("""
            SELECT po.orderDate AS orderDate, COALESCE(SUM(po.totalAmount), 0) AS totalPurchase
            FROM PurchaseOrder po
            WHERE po.orderDate BETWEEN :from AND :to
              AND UPPER(po.status) = 'CONFIRMED'
            GROUP BY po.orderDate
            ORDER BY po.orderDate
            """)
    List<DailyPurchaseTrendProjection> findDailyPurchaseTrend(@Param("from") LocalDate from, @Param("to") LocalDate to);

    /** Projection for daily trend aggregation query. */
    interface DailyPurchaseTrendProjection {
        LocalDate getOrderDate();
        BigDecimal getTotalPurchase();
    }

    /** Aggregates purchased units by product for a date range. */
    @Query("""
            SELECT p.productId AS productId, p.productName AS productName,
                   COALESCE(SUM(oi.quantity * v.piecesPerPack), 0) AS totalUnits
            FROM PurchaseOrder po
            JOIN po.items oi
            JOIN oi.variant v
            JOIN v.product p
            WHERE po.orderDate BETWEEN :from AND :to
              AND UPPER(po.status) = 'CONFIRMED'
            GROUP BY p.productId, p.productName
            ORDER BY totalUnits DESC, p.productName ASC
            """)
    List<ProductUnitsProjection> findProductUnitsByDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    /** Aggregates purchased units by variant for a date range. */
    @Query("""
            SELECT p.productId AS productId, v.variantId AS variantId, v.variantLabel AS variantLabel,
                   COALESCE(SUM(oi.quantity * v.piecesPerPack), 0) AS totalUnits
            FROM PurchaseOrder po
            JOIN po.items oi
            JOIN oi.variant v
            JOIN v.product p
            WHERE po.orderDate BETWEEN :from AND :to
              AND UPPER(po.status) = 'CONFIRMED'
            GROUP BY p.productId, v.variantId, v.variantLabel
            ORDER BY p.productId ASC, totalUnits DESC, v.variantLabel ASC
            """)
    List<VariantUnitsProjection> findVariantUnitsByDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    /** Aggregates seller-wise total purchase by date range in descending order. */
    @Query("""
            SELECT s.sellerId AS sellerId, s.name AS sellerName,
                   COALESCE(SUM(po.totalAmount), 0) AS totalPurchase
            FROM PurchaseOrder po
            JOIN po.seller s
            WHERE po.orderDate BETWEEN :from AND :to
              AND UPPER(po.status) = 'CONFIRMED'
            GROUP BY s.sellerId, s.name
            ORDER BY totalPurchase DESC, s.name ASC
            """)
    List<SellerPurchaseProjection> findTopSellersByPurchase(@Param("from") LocalDate from, @Param("to") LocalDate to);

    /** Projection for product-level purchased units. */
    interface ProductUnitsProjection {
        Long getProductId();
        String getProductName();
        BigDecimal getTotalUnits();
    }

    /** Projection for variant-level purchased units. */
    interface VariantUnitsProjection {
        Long getProductId();
        Long getVariantId();
        String getVariantLabel();
        BigDecimal getTotalUnits();
    }

    /** Projection for seller-level purchase totals. */
    interface SellerPurchaseProjection {
        Long getSellerId();
        String getSellerName();
        BigDecimal getTotalPurchase();
    }
}
