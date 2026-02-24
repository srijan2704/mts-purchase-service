package com.mts.mts_purchase_service.repository;

import com.mts.mts_purchase_service.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Unit repository with duplicate-name checks.
 */
@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {

    /** Returns units sorted by name. */
    List<Unit> findAllByOrderByUnitNameAsc();

    /** Checks duplicate name for create flow. */
    @Query("SELECT COUNT(u) > 0 FROM Unit u WHERE LOWER(u.unitName) = LOWER(:unitName)")
    boolean existsByUnitNameIgnoreCase(@Param("unitName") String unitName);

    /** Checks duplicate name excluding current id for update flow. */
    @Query("SELECT COUNT(u) > 0 FROM Unit u WHERE LOWER(u.unitName) = LOWER(:unitName) AND u.unitId <> :id")
    boolean existsByUnitNameIgnoreCaseAndIdNot(@Param("unitName") String unitName, @Param("id") Long id);
}
