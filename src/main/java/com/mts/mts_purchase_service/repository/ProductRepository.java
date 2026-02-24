package com.mts.mts_purchase_service.repository;

import com.mts.mts_purchase_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for product master records.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Search products with optional filters for type and name.
     */
    @Query("""
           SELECT p FROM Product p
           WHERE (:includeInactive = true OR p.isActive = 1)
             AND (:typeId IS NULL OR p.type.typeId = :typeId)
             AND (:search IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :search, '%')))
           ORDER BY p.productName ASC
           """)
    List<Product> search(
            @Param("typeId") Long typeId,
            @Param("search") String search,
            @Param("includeInactive") boolean includeInactive
    );

    /**
     * Checks duplicate name for create operation.
     */
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE LOWER(p.productName) = LOWER(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    /**
     * Checks duplicate name for update operation excluding current record.
     */
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE LOWER(p.productName) = LOWER(:name) AND p.productId <> :id")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("id") Long id);
}
