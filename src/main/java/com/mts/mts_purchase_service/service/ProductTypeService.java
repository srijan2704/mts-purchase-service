package com.mts.mts_purchase_service.service;

import com.mts.mts_purchase_service.models.ProductTypeDTO;

import java.util.List;

/**
 * Product type service contract.
 */
public interface ProductTypeService {

    /** Returns all product types. */
    List<ProductTypeDTO> getAllProductTypes();

    /** Returns one product type by id. */
    ProductTypeDTO getProductTypeById(Long id);

    /** Creates one product type. */
    ProductTypeDTO createProductType(ProductTypeDTO productTypeDTO);

    /** Updates one product type. */
    ProductTypeDTO updateProductType(Long id, ProductTypeDTO productTypeDTO);

    /** Deletes one product type. */
    void deleteProductType(Long id);
}
