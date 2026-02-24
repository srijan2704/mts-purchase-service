package com.mts.mts_purchase_service.service;

import com.mts.mts_purchase_service.models.UnitDTO;

import java.util.List;

/**
 * Unit service contract.
 */
public interface UnitService {

    /** Returns all units. */
    List<UnitDTO> getAllUnits();

    /** Returns one unit by id. */
    UnitDTO getUnitById(Long id);

    /** Creates one unit. */
    UnitDTO createUnit(UnitDTO unitDTO);

    /** Updates one unit. */
    UnitDTO updateUnit(Long id, UnitDTO unitDTO);

    /** Deletes one unit. */
    void deleteUnit(Long id);
}
