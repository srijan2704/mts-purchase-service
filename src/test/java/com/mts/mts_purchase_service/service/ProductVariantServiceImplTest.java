package com.mts.mts_purchase_service.service;

import com.mts.mts_purchase_service.entity.Product;
import com.mts.mts_purchase_service.entity.ProductType;
import com.mts.mts_purchase_service.entity.ProductVariant;
import com.mts.mts_purchase_service.entity.Unit;
import com.mts.mts_purchase_service.exception.DuplicateResourceException;
import com.mts.mts_purchase_service.exception.ResourceNotFoundException;
import com.mts.mts_purchase_service.models.ProductVariantDTO;
import com.mts.mts_purchase_service.repository.ProductRepository;
import com.mts.mts_purchase_service.repository.ProductVariantRepository;
import com.mts.mts_purchase_service.repository.UnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ProductVariantServiceImpl business rules.
 */
@ExtendWith(MockitoExtension.class)
class ProductVariantServiceImplTest {

    @Mock
    private ProductVariantRepository productVariantRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UnitRepository unitRepository;

    private ProductVariantServiceImpl productVariantService;

    @BeforeEach
    void setUp() {
        productVariantService = new ProductVariantServiceImpl(productVariantRepository, productRepository, unitRepository);
    }

    @Test
    void addVariant_shouldCreateWhenUnique() {
        ProductType type = new ProductType(2L, "Oils", "desc");
        Product product = new Product();
        product.setProductId(10L);
        product.setProductName("Mangal Oil");
        product.setType(type);

        Unit unit = new Unit(1L, "Litre", "L", "Liquid unit");

        ProductVariantDTO request = new ProductVariantDTO(null, null, 1L, null,
                "1L x 16 Box", new BigDecimal("1.000"), 16, null, true);

        ProductVariant saved = new ProductVariant();
        saved.setVariantId(77L);
        saved.setProduct(product);
        saved.setUnit(unit);
        saved.setVariantLabel("1L x 16 Box");
        saved.setPackSize(new BigDecimal("1.000"));
        saved.setPiecesPerPack(16);
        saved.setActive(true);

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(productVariantRepository.existsByProductIdAndLabelIgnoreCase(10L, "1L x 16 Box")).thenReturn(false);
        when(productVariantRepository.save(any(ProductVariant.class))).thenReturn(saved);

        ProductVariantDTO response = productVariantService.addVariant(10L, request);

        assertEquals(77L, response.getVariantId());
        assertEquals("1L x 16 Box", response.getVariantLabel());
    }

    @Test
    void addVariant_shouldThrowOnDuplicateLabel() {
        Product product = new Product();
        product.setProductId(10L);
        Unit unit = new Unit(1L, "Litre", "L", "Liquid unit");

        ProductVariantDTO request = new ProductVariantDTO(null, null, 1L, null,
                "1L x 16 Box", new BigDecimal("1.000"), 16, null, true);

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(productVariantRepository.existsByProductIdAndLabelIgnoreCase(10L, "1L x 16 Box")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> productVariantService.addVariant(10L, request));
    }

    @Test
    void getVariantsByProduct_shouldThrowWhenProductMissing() {
        when(productRepository.existsById(404L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> productVariantService.getVariantsByProduct(404L, false));
    }

    @Test
    void updateVariant_shouldThrowOnDuplicateLabelInSameProduct() {
        Product product = new Product();
        product.setProductId(10L);

        Unit oldUnit = new Unit(1L, "Litre", "L", "Liquid unit");
        ProductVariant existing = new ProductVariant();
        existing.setVariantId(77L);
        existing.setProduct(product);
        existing.setUnit(oldUnit);
        existing.setVariantLabel("Old Label");
        existing.setPackSize(new BigDecimal("1.000"));
        existing.setPiecesPerPack(16);

        Unit newUnit = new Unit(2L, "Piece", "pcs", "Piece unit");

        ProductVariantDTO request = new ProductVariantDTO(null, null, 2L, null,
                "Duplicate Label", new BigDecimal("2.000"), 12, null, true);

        when(productVariantRepository.findById(77L)).thenReturn(Optional.of(existing));
        when(unitRepository.findById(2L)).thenReturn(Optional.of(newUnit));
        when(productVariantRepository.existsByProductIdAndLabelIgnoreCaseAndVariantIdNot(10L, "Duplicate Label", 77L))
                .thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> productVariantService.updateVariant(77L, request));
    }

    @Test
    void softDeleteVariant_shouldMarkInactiveAndSave() {
        ProductVariant existing = new ProductVariant();
        existing.setVariantId(77L);
        existing.setActive(true);

        when(productVariantRepository.findById(77L)).thenReturn(Optional.of(existing));

        productVariantService.softDeleteVariant(77L);

        verify(productVariantRepository).save(argThat(variant -> !variant.isActive()));
    }
}
