package com.mts.mts_purchase_service.service;

import com.mts.mts_purchase_service.entity.Unit;
import com.mts.mts_purchase_service.exception.DuplicateResourceException;
import com.mts.mts_purchase_service.exception.ResourceNotFoundException;
import com.mts.mts_purchase_service.models.UnitDTO;
import com.mts.mts_purchase_service.repository.UnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnitServiceImplTest {

    @Mock
    private UnitRepository unitRepository;

    private UnitServiceImpl unitService;

    @BeforeEach
    void setUp() {
        unitService = new UnitServiceImpl(unitRepository);
    }

    @Test
    void createUnit_shouldCreateWhenNameUnique() {
        UnitDTO request = new UnitDTO(null, "Kilogram", "kg", "Weight unit");
        Unit savedEntity = new Unit(1L, "Kilogram", "kg", "Weight unit");

        when(unitRepository.existsByUnitNameIgnoreCase("Kilogram")).thenReturn(false);
        when(unitRepository.save(org.mockito.ArgumentMatchers.any(Unit.class))).thenReturn(savedEntity);

        UnitDTO response = unitService.createUnit(request);

        assertEquals(1L, response.getUnitId());
        assertEquals("Kilogram", response.getUnitName());
        verify(unitRepository).save(org.mockito.ArgumentMatchers.any(Unit.class));
    }

    @Test
    void createUnit_shouldThrowWhenDuplicateName() {
        UnitDTO request = new UnitDTO(null, "Kilogram", "kg", "Weight unit");
        when(unitRepository.existsByUnitNameIgnoreCase("Kilogram")).thenReturn(true);

        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> unitService.createUnit(request)
        );

        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void getUnitById_shouldThrowWhenNotFound() {
        when(unitRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> unitService.getUnitById(99L));
    }
}
