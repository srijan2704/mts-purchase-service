package com.mts.mts_purchase_service.service;

import com.mts.mts_purchase_service.entity.ProductType;
import com.mts.mts_purchase_service.exception.DuplicateResourceException;
import com.mts.mts_purchase_service.exception.ResourceNotFoundException;
import com.mts.mts_purchase_service.models.ProductTypeDTO;
import com.mts.mts_purchase_service.repository.ProductTypeRepository;
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
class ProductTypeServiceImplTest {

    @Mock
    private ProductTypeRepository productTypeRepository;

    private ProductTypeServiceImpl productTypeService;

    @BeforeEach
    void setUp() {
        productTypeService = new ProductTypeServiceImpl(productTypeRepository);
    }

    @Test
    void createProductType_shouldCreateWhenNameUnique() {
        ProductTypeDTO request = new ProductTypeDTO(null, "Edible Oils", "Oils category");
        ProductType savedEntity = new ProductType(1L, "Edible Oils", "Oils category");

        when(productTypeRepository.existsByTypeNameIgnoreCase("Edible Oils")).thenReturn(false);
        when(productTypeRepository.save(org.mockito.ArgumentMatchers.any(ProductType.class))).thenReturn(savedEntity);

        ProductTypeDTO response = productTypeService.createProductType(request);

        assertEquals(1L, response.getTypeId());
        assertEquals("Edible Oils", response.getTypeName());
        verify(productTypeRepository).save(org.mockito.ArgumentMatchers.any(ProductType.class));
    }

    @Test
    void createProductType_shouldThrowWhenDuplicateName() {
        ProductTypeDTO request = new ProductTypeDTO(null, "Edible Oils", "Oils category");
        when(productTypeRepository.existsByTypeNameIgnoreCase("Edible Oils")).thenReturn(true);

        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> productTypeService.createProductType(request)
        );

        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void getProductTypeById_shouldThrowWhenNotFound() {
        when(productTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productTypeService.getProductTypeById(99L));
    }
}
