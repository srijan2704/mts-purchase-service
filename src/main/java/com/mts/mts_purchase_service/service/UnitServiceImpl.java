package com.mts.mts_purchase_service.service;

import com.mts.mts_purchase_service.entity.Unit;
import com.mts.mts_purchase_service.exception.DuplicateResourceException;
import com.mts.mts_purchase_service.exception.ResourceNotFoundException;
import com.mts.mts_purchase_service.models.UnitDTO;
import com.mts.mts_purchase_service.repository.UnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for unit master data.
 */
@Service
public class UnitServiceImpl implements UnitService {

    private final UnitRepository unitRepository;

    /** Injects repository dependency for unit operations. */
    public UnitServiceImpl(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }

    /** Returns all units sorted by name. */
    @Override
    @Transactional(readOnly = true)
    public List<UnitDTO> getAllUnits() {
        return unitRepository.findAllByOrderByUnitNameAsc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /** Returns one unit by id. */
    @Override
    @Transactional(readOnly = true)
    public UnitDTO getUnitById(Long id) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", id));
        return mapToDTO(unit);
    }

    /** Creates a new unit after duplicate-name validation. */
    @Override
    @Transactional
    public UnitDTO createUnit(UnitDTO unitDTO) {
        boolean exists = unitRepository.existsByUnitNameIgnoreCase(unitDTO.getUnitName());
        if (exists) {
            throw new DuplicateResourceException("Unit with name '" + unitDTO.getUnitName() + "' already exists");
        }

        Unit saved = unitRepository.save(mapToEntity(unitDTO));
        return mapToDTO(saved);
    }

    /** Updates existing unit after duplicate-name validation. */
    @Override
    @Transactional
    public UnitDTO updateUnit(Long id, UnitDTO unitDTO) {
        Unit existing = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", id));

        boolean clash = unitRepository.existsByUnitNameIgnoreCaseAndIdNot(unitDTO.getUnitName(), id);
        if (clash) {
            throw new DuplicateResourceException("Another unit with name '" + unitDTO.getUnitName() + "' already exists");
        }

        existing.setUnitName(unitDTO.getUnitName());
        existing.setAbbreviation(unitDTO.getAbbreviation());
        existing.setDescription(unitDTO.getDescription());

        Unit saved = unitRepository.save(existing);
        return mapToDTO(saved);
    }

    /** Deletes one unit after existence validation. */
    @Override
    @Transactional
    public void deleteUnit(Long id) {
        if (!unitRepository.existsById(id)) {
            throw new ResourceNotFoundException("Unit", id);
        }
        unitRepository.deleteById(id);
    }

    /** Maps entity to API DTO. */
    private UnitDTO mapToDTO(Unit unit) {
        return new UnitDTO(unit.getUnitId(), unit.getUnitName(), unit.getAbbreviation(), unit.getDescription());
    }

    /** Maps create/update request DTO to entity. */
    private Unit mapToEntity(UnitDTO dto) {
        Unit unit = new Unit();
        unit.setUnitName(dto.getUnitName());
        unit.setAbbreviation(dto.getAbbreviation());
        unit.setDescription(dto.getDescription());
        return unit;
    }
}
