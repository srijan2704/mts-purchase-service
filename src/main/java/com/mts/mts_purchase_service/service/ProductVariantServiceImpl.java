package com.mts.mts_purchase_service.service;

import com.mts.mts_purchase_service.entity.Product;
import com.mts.mts_purchase_service.entity.ProductVariant;
import com.mts.mts_purchase_service.entity.Unit;
import com.mts.mts_purchase_service.exception.DuplicateResourceException;
import com.mts.mts_purchase_service.exception.ResourceNotFoundException;
import com.mts.mts_purchase_service.models.ProductVariantDTO;
import com.mts.mts_purchase_service.repository.ProductRepository;
import com.mts.mts_purchase_service.repository.ProductVariantRepository;
import com.mts.mts_purchase_service.repository.UnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles product variant business rules and persistence.
 */
@Service
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final UnitRepository unitRepository;

    public ProductVariantServiceImpl(ProductVariantRepository productVariantRepository,
                                     ProductRepository productRepository,
                                     UnitRepository unitRepository) {
        this.productVariantRepository = productVariantRepository;
        this.productRepository = productRepository;
        this.unitRepository = unitRepository;
    }

    /**
     * Returns variants for one product and supports active-only or all behavior.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductVariantDTO> getVariantsByProduct(Long productId, boolean includeInactive) {
        List<ProductVariant> variants = includeInactive
                ? productVariantRepository.findByProductProductIdOrderByVariantLabelAsc(productId)
                : productVariantRepository.findByProductProductIdAndIsActiveOrderByVariantLabelAsc(productId, 1);

        // Preserve not-found behavior without paying existence check cost for normal non-empty results.
        if (variants.isEmpty() && !productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", productId);
        }

        return variants.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    /**
     * Returns variants for multiple products in one query to avoid N+1 round-trips.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductVariantDTO> getVariantsByProductIds(List<Long> productIds, boolean includeInactive) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }

        List<ProductVariant> variants = productVariantRepository.findByProductIdsWithRefs(productIds, includeInactive);
        return variants.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    /**
     * Returns one variant by id for order edit/view scenarios.
     */
    @Override
    @Transactional(readOnly = true)
    public ProductVariantDTO getVariantById(Long variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", variantId));
        return mapToDTO(variant);
    }

    /**
     * Adds a variant after validating product/unit references and duplicate label constraints.
     */
    @Override
    @Transactional
    public ProductVariantDTO addVariant(Long productId, ProductVariantDTO variantDTO) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        Unit unit = unitRepository.findById(variantDTO.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit", variantDTO.getUnitId()));

        boolean duplicate = productVariantRepository.existsByProductIdAndLabelIgnoreCase(productId, variantDTO.getVariantLabel());
        if (duplicate) {
            throw new DuplicateResourceException("Variant label '" + variantDTO.getVariantLabel() + "' already exists for this product");
        }

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setUnit(unit);
        variant.setVariantLabel(variantDTO.getVariantLabel());
        variant.setPackSize(variantDTO.getPackSize());
        variant.setPiecesPerPack(variantDTO.getPiecesPerPack());
        variant.setBarcode(variantDTO.getBarcode());
        // New variants are active by default.
        variant.setActive(true);

        ProductVariant saved = productVariantRepository.save(variant);
        return mapToDTO(saved);
    }

    /**
     * Updates variant attributes while preserving product ownership rules.
     */
    @Override
    @Transactional
    public ProductVariantDTO updateVariant(Long variantId, ProductVariantDTO variantDTO) {
        ProductVariant existing = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", variantId));

        Long productId = existing.getProduct().getProductId();

        Unit unit = unitRepository.findById(variantDTO.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit", variantDTO.getUnitId()));

        boolean duplicate = productVariantRepository.existsByProductIdAndLabelIgnoreCaseAndVariantIdNot(
                productId, variantDTO.getVariantLabel(), variantId
        );
        if (duplicate) {
            throw new DuplicateResourceException("Another variant with label '" + variantDTO.getVariantLabel() + "' already exists for this product");
        }

        existing.setUnit(unit);
        existing.setVariantLabel(variantDTO.getVariantLabel());
        existing.setPackSize(variantDTO.getPackSize());
        existing.setPiecesPerPack(variantDTO.getPiecesPerPack());
        existing.setBarcode(variantDTO.getBarcode());

        ProductVariant saved = productVariantRepository.save(existing);
        return mapToDTO(saved);
    }

    /**
     * Soft-deletes variant so historical order references remain valid.
     */
    @Override
    @Transactional
    public void softDeleteVariant(Long variantId) {
        ProductVariant existing = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", variantId));

        existing.setActive(false);
        productVariantRepository.save(existing);
    }

    /** Maps entity to API DTO including unit display data. */
    private ProductVariantDTO mapToDTO(ProductVariant variant) {
        return new ProductVariantDTO(
                variant.getVariantId(),
                variant.getProduct().getProductId(),
                variant.getUnit().getUnitId(),
                variant.getUnit().getUnitName(),
                variant.getVariantLabel(),
                variant.getPackSize(),
                variant.getPiecesPerPack(),
                variant.getBarcode(),
                variant.isActive()
        );
    }
}
