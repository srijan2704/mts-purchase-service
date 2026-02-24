package com.mts.mts_purchase_service.service;

import com.mts.mts_purchase_service.models.ProductDTO;

import java.util.List;

/**
 * Product master service contract.
 */
public interface ProductService {

    /** Returns products with optional type/search filtering. */
    List<ProductDTO> getProducts(Long typeId, String search, boolean includeInactive, boolean includeVariants);

    /** Returns one product by id. */
    ProductDTO getProductById(Long id);

    /** Creates product and optional initial variants. */
    ProductDTO createProduct(ProductDTO productDTO);

    /** Updates product-level fields (name, type, description). */
    ProductDTO updateProduct(Long id, ProductDTO productDTO);

    /** Soft-deletes product by setting active flag to false. */
    void softDeleteProduct(Long id);
}
