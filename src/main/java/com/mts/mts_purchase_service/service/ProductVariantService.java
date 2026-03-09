package com.mts.mts_purchase_service.service;

import com.mts.mts_purchase_service.models.ProductVariantDTO;

import java.util.List;

/**
 * Product variant service contract.
 */
public interface ProductVariantService {

    /** Returns variants for a product. */
    List<ProductVariantDTO> getVariantsByProduct(Long productId, boolean includeInactive);

    /** Returns variants for multiple products in one call. */
    List<ProductVariantDTO> getVariantsByProductIds(List<Long> productIds, boolean includeInactive);

    /** Returns one variant by id. */
    ProductVariantDTO getVariantById(Long variantId);

    /** Creates a new variant under one product. */
    ProductVariantDTO addVariant(Long productId, ProductVariantDTO variantDTO);

    /** Updates an existing variant. */
    ProductVariantDTO updateVariant(Long variantId, ProductVariantDTO variantDTO);

    /** Soft-deletes variant by setting active flag to false. */
    void softDeleteVariant(Long variantId);
}
