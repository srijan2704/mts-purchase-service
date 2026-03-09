package com.mts.mts_purchase_service.service;

import com.mts.mts_purchase_service.entity.Product;
import com.mts.mts_purchase_service.entity.ProductType;
import com.mts.mts_purchase_service.exception.DuplicateResourceException;
import com.mts.mts_purchase_service.exception.ResourceNotFoundException;
import com.mts.mts_purchase_service.models.ProductDTO;
import com.mts.mts_purchase_service.models.ProductVariantDTO;
import com.mts.mts_purchase_service.repository.ProductRepository;
import com.mts.mts_purchase_service.repository.ProductTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ProductServiceImpl business rules.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductTypeRepository productTypeRepository;
    @Mock
    private ProductVariantService productVariantService;

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository, productTypeRepository, productVariantService);
    }

    @Test
    void createProduct_shouldCreateWithInitialVariants() {
        ProductType type = new ProductType(3L, "Edible Oils", "desc");

        ProductDTO request = new ProductDTO();
        request.setProductName("Mangal Sunflower Oil");
        request.setTypeId(3L);
        request.setDescription("Refined oil");
        request.setVariants(List.of(
                new ProductVariantDTO(null, null, 1L, null, "1L x 16 Box", new BigDecimal("1.000"), 16, null, true)
        ));

        Product saved = new Product();
        saved.setProductId(10L);
        saved.setProductName("Mangal Sunflower Oil");
        saved.setType(type);
        saved.setDescription("Refined oil");
        saved.setActive(true);

        ProductVariantDTO createdVariant = new ProductVariantDTO(1L, 10L, 1L, "Litre", "1L x 16 Box", new BigDecimal("1.000"), 16, null, true);

        when(productRepository.existsByNameIgnoreCase("Mangal Sunflower Oil")).thenReturn(false);
        when(productTypeRepository.findById(3L)).thenReturn(Optional.of(type));
        when(productRepository.save(any(Product.class))).thenReturn(saved);
        when(productVariantService.addVariant(org.mockito.ArgumentMatchers.eq(10L), any(ProductVariantDTO.class))).thenReturn(createdVariant);

        ProductDTO response = productService.createProduct(request);

        assertEquals(10L, response.getProductId());
        assertEquals(1, response.getVariants().size());
        assertEquals("1L x 16 Box", response.getVariants().get(0).getVariantLabel());
    }

    @Test
    void createProduct_shouldThrowOnDuplicateName() {
        ProductDTO request = new ProductDTO();
        request.setProductName("Mangal Sunflower Oil");

        when(productRepository.existsByNameIgnoreCase("Mangal Sunflower Oil")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> productService.createProduct(request));
    }

    @Test
    void getProductById_shouldThrowWhenNotFound() {
        when(productRepository.findById(88L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(88L));
    }

    @Test
    void updateProduct_shouldThrowWhenDuplicateNameExists() {
        ProductType type = new ProductType(3L, "Edible Oils", "desc");
        Product existing = new Product();
        existing.setProductId(10L);
        existing.setProductName("Old Name");
        existing.setType(type);

        ProductDTO request = new ProductDTO();
        request.setProductName("Mangal Sunflower Oil");
        request.setTypeId(3L);
        request.setDescription("Updated");

        when(productRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(productRepository.existsByNameIgnoreCaseAndIdNot("Mangal Sunflower Oil", 10L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> productService.updateProduct(10L, request));
    }

    @Test
    void softDeleteProduct_shouldMarkInactiveAndSave() {
        ProductType type = new ProductType(3L, "Edible Oils", "desc");
        Product existing = new Product();
        existing.setProductId(10L);
        existing.setProductName("Mangal Sunflower Oil");
        existing.setType(type);
        existing.setActive(true);

        when(productRepository.findById(10L)).thenReturn(Optional.of(existing));

        productService.softDeleteProduct(10L);

        verify(productRepository).save(argThat(product -> !product.isActive()));
    }

    @Test
    void getProducts_withIncludeVariantsTrue_shouldUseBulkVariantFetch() {
        ProductType type = new ProductType(3L, "Edible Oils", "desc");

        Product p1 = new Product();
        p1.setProductId(10L);
        p1.setProductName("Mangal Sunflower Oil");
        p1.setType(type);
        p1.setActive(true);

        Product p2 = new Product();
        p2.setProductId(11L);
        p2.setProductName("Mangal Mustard Oil");
        p2.setType(type);
        p2.setActive(true);

        ProductVariantDTO v1 = new ProductVariantDTO(101L, 10L, 1L, "Litre",
                "1L x 16 Box", new BigDecimal("1.000"), 16, null, true);
        ProductVariantDTO v2 = new ProductVariantDTO(102L, 11L, 1L, "Litre",
                "5L x 4 Box", new BigDecimal("5.000"), 4, null, true);

        when(productRepository.search(null, null, false)).thenReturn(List.of(p1, p2));
        when(productVariantService.getVariantsByProductIds(anyList(), eq(false))).thenReturn(List.of(v1, v2));

        List<ProductDTO> response = productService.getProducts(null, null, false, true);

        assertEquals(2, response.size());
        assertEquals(1, response.get(0).getVariants().size());
        assertEquals(1, response.get(1).getVariants().size());
        verify(productVariantService).getVariantsByProductIds(argThat(ids -> ids.size() == 2 && ids.containsAll(List.of(10L, 11L))), eq(false));
        verify(productVariantService, never()).getVariantsByProduct(any(Long.class), eq(false));
    }

    @Test
    void getProducts_withIncludeVariantsFalse_shouldNotFetchVariants() {
        ProductType type = new ProductType(3L, "Edible Oils", "desc");
        Product p1 = new Product();
        p1.setProductId(10L);
        p1.setProductName("Mangal Sunflower Oil");
        p1.setType(type);
        p1.setActive(true);

        when(productRepository.search(null, null, false)).thenReturn(List.of(p1));

        List<ProductDTO> response = productService.getProducts(null, null, false, false);

        assertEquals(1, response.size());
        assertEquals(0, response.get(0).getVariants().size());
        verify(productVariantService, never()).getVariantsByProductIds(anyList(), eq(false));
    }
}
