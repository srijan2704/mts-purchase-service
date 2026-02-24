package com.mts.mts_purchase_service.service;


import com.mts.mts_purchase_service.models.SellerDTO;

import java.util.List;

/**
 * Service interface for Seller business logic.
 * The controller depends on this interface — not the implementation —
 * keeping the layers loosely coupled.
 *
 * Implementation class: SellerServiceImpl.java
 */
public interface SellerService {

    /**
     * Returns all sellers where is_active = 1.
     * Used to populate the seller dropdown in the Purchase Order form.
     */
    List<SellerDTO> getAllActiveSellers();

    /**
     * Returns all sellers regardless of active status.
     * Used in the admin / seller management screen.
     */
    List<SellerDTO> getAllSellers();

    /**
     * Fetch a single seller by primary key.
     * Throws ResourceNotFoundException if not found.
     */
    SellerDTO getSellerById(Long id);

    /**
     * Case-insensitive partial search on seller name.
     * e.g. "sharma" matches "Sharma Traders", "Sharmaji & Co."
     */
    List<SellerDTO> searchSellersByName(String name);

    /**
     * Creates a new seller. is_active defaults to true.
     * Throws DuplicateResourceException if seller name already exists.
     */
    SellerDTO createSeller(SellerDTO sellerDTO);

    /**
     * Full update of a seller record.
     * Throws ResourceNotFoundException if seller not found.
     */
    SellerDTO updateSeller(Long id, SellerDTO sellerDTO);

    /**
     * Activate or deactivate a seller.
     * Used by the toggle switch on the UI.
     */
    SellerDTO updateSellerStatus(Long id, boolean active);

    /**
     * Soft-delete: sets is_active = 0.
     * The seller record is preserved for historical order integrity.
     * Hard delete is not supported.
     */
    void softDeleteSeller(Long id);
}
