package com.mts.mts_purchase_service.repository;

import com.mts.mts_purchase_service.entity.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Product type repository with duplicate-name checks.
 */
@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {

    /** Returns product types sorted by name. */
    List<ProductType> findAllByOrderByTypeNameAsc();

    /** Checks duplicate name for create flow. */
    @Query("SELECT COUNT(pt) > 0 FROM ProductType pt WHERE LOWER(pt.typeName) = LOWER(:typeName)")
    boolean existsByTypeNameIgnoreCase(@Param("typeName") String typeName);

    /** Checks duplicate name excluding current id for update flow. */
    @Query("SELECT COUNT(pt) > 0 FROM ProductType pt WHERE LOWER(pt.typeName) = LOWER(:typeName) AND pt.typeId <> :id")
    boolean existsByTypeNameIgnoreCaseAndIdNot(@Param("typeName") String typeName, @Param("id") Long id);
}
