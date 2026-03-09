package com.mts.mts_purchase_service.repository;

import com.mts.mts_purchase_service.entity.ProductVariant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for product variant records.
 */
@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    /**
     * Returns active variants for one product sorted by label.
     */
    @EntityGraph(attributePaths = {"product", "unit"})
    List<ProductVariant> findByProductProductIdAndIsActiveOrderByVariantLabelAsc(Long productId, int isActive);

    /**
     * Returns all variants for one product sorted by label.
     */
    @EntityGraph(attributePaths = {"product", "unit"})
    List<ProductVariant> findByProductProductIdOrderByVariantLabelAsc(Long productId);

    /**
     * Returns variants for multiple products in one query with required references preloaded.
     */
    @Query("""
           SELECT v FROM ProductVariant v
           JOIN FETCH v.product p
           JOIN FETCH v.unit u
           WHERE p.productId IN :productIds
             AND (:includeInactive = true OR v.isActive = 1)
           ORDER BY p.productId ASC, v.variantLabel ASC
           """)
    List<ProductVariant> findByProductIdsWithRefs(
            @Param("productIds") List<Long> productIds,
            @Param("includeInactive") boolean includeInactive
    );

    /**
     * Checks duplicate variant label inside a single product.
     */
    @Query("""
           SELECT COUNT(v) > 0 FROM ProductVariant v
           WHERE v.product.productId = :productId
             AND LOWER(v.variantLabel) = LOWER(:label)
           """)
    boolean existsByProductIdAndLabelIgnoreCase(@Param("productId") Long productId, @Param("label") String label);

    /**
     * Checks duplicate variant label excluding current variant while updating.
     */
    @Query("""
           SELECT COUNT(v) > 0 FROM ProductVariant v
           WHERE v.product.productId = :productId
             AND LOWER(v.variantLabel) = LOWER(:label)
             AND v.variantId <> :variantId
           """)
    boolean existsByProductIdAndLabelIgnoreCaseAndVariantIdNot(
            @Param("productId") Long productId,
            @Param("label") String label,
            @Param("variantId") Long variantId
    );
}
