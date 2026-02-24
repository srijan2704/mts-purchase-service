package com.mts.mts_purchase_service.service;


import com.mts.mts_purchase_service.models.SellerDTO;
import com.mts.mts_purchase_service.entity.Seller;
import com.mts.mts_purchase_service.exception.DuplicateResourceException;
import com.mts.mts_purchase_service.exception.ResourceNotFoundException;
import com.mts.mts_purchase_service.repository.SellerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of SellerService.
 *
 * Responsibilities:
 *   - Business logic and validations (duplicate check, not-found check)
 *   - Mapping between Seller entity and SellerDTO
 *   - Delegating persistence to SellerRepository
 *
 * All public methods that write to DB are annotated @Transactional.
 * Read-only methods use @Transactional(readOnly = true) for performance.
 */
@Service
public class SellerServiceImpl implements SellerService {

    private static final Logger log = LoggerFactory.getLogger(SellerServiceImpl.class);

    private final SellerRepository sellerRepository;

    public SellerServiceImpl(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
    }

    // ─────────────────────────────────────────────────────────────
    // GET: All active sellers
    // ─────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<SellerDTO> getAllActiveSellers() {
        log.info("Fetching all active sellers from database");

        List<Seller> sellers = sellerRepository.findByIsActiveOrderByNameAsc(1);

        log.info("{} active seller(s) found in database", sellers.size());
        return sellers.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    // GET: All sellers (active + inactive)
    // ─────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<SellerDTO> getAllSellers() {
        log.info("Fetching all sellers (active + inactive) from database");

        List<Seller> sellers = sellerRepository.findAllByOrderByNameAsc();

        log.info("{} total seller(s) found in database", sellers.size());
        return sellers.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    // GET: Single seller by ID
    // ─────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public SellerDTO getSellerById(Long id) {
        log.info("Fetching seller by id={}", id);

        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> {
                    log.info("Seller not found with id={}", id);
                    return new ResourceNotFoundException("Seller", id);
                });

        log.info("Seller found - id={}, name='{}'", seller.getSellerId(), seller.getName());
        return mapToDTO(seller);
    }

    // ─────────────────────────────────────────────────────────────
    // GET: Search by name
    // ─────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<SellerDTO> searchSellersByName(String name) {
        log.info("Searching sellers with keyword='{}'", name);

        List<Seller> sellers = sellerRepository.searchByNameIgnoreCase(name);

        log.info("{} seller(s) matched keyword='{}'", sellers.size(), name);
        return sellers.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    // POST: Create new seller
    // ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public SellerDTO createSeller(SellerDTO sellerDTO) {
        log.info("Creating new seller with name='{}'", sellerDTO.getName());

        // Check for duplicate seller name (case-insensitive)
        boolean nameExists = sellerRepository.existsByNameIgnoreCase(sellerDTO.getName());
        if (nameExists) {
            log.info("Duplicate seller name detected: '{}'", sellerDTO.getName());
            throw new DuplicateResourceException(
                "Seller with name '" + sellerDTO.getName() + "' already exists"
            );
        }

        Seller seller = mapToEntity(sellerDTO);
        Seller savedSeller = sellerRepository.save(seller);

        log.info("Seller created successfully - id={}, name='{}'",
                savedSeller.getSellerId(), savedSeller.getName());
        return mapToDTO(savedSeller);
    }

    // ─────────────────────────────────────────────────────────────
    // PUT: Update existing seller (full update)
    // ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public SellerDTO updateSeller(Long id, SellerDTO sellerDTO) {
        log.info("Updating seller id={}, new name='{}'", id, sellerDTO.getName());

        // Check seller exists
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> {
                    log.info("Update failed - seller not found with id={}", id);
                    return new ResourceNotFoundException("Seller", id);
                });

        // Check if updated name clashes with another seller's name
        boolean nameClash = sellerRepository.existsByNameIgnoreCaseAndIdNot(
                sellerDTO.getName(), id
        );
        if (nameClash) {
            log.info("Update failed - seller name '{}' already used by another seller",
                    sellerDTO.getName());
            throw new DuplicateResourceException(
                "Another seller with name '" + sellerDTO.getName() + "' already exists"
            );
        }

        // Apply updates
        seller.setName(sellerDTO.getName());
        seller.setContactPerson(sellerDTO.getContactPerson());
        seller.setPhone(sellerDTO.getPhone());
        seller.setEmail(sellerDTO.getEmail());
        seller.setAddress(sellerDTO.getAddress());
        seller.setGstNumber(sellerDTO.getGstNumber());

        Seller updatedSeller = sellerRepository.save(seller);

        log.info("Seller updated successfully - id={}, name='{}'",
                updatedSeller.getSellerId(), updatedSeller.getName());
        return mapToDTO(updatedSeller);
    }

    // ─────────────────────────────────────────────────────────────
    // PATCH: Activate or deactivate seller
    // ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public SellerDTO updateSellerStatus(Long id, boolean active) {
        log.info("Updating status of seller id={} to active={}", id, active);

        // Verify seller exists before toggling
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> {
                    log.info("Status update failed - seller not found with id={}", id);
                    return new ResourceNotFoundException("Seller", id);
                });

        int statusValue = active ? 1 : 0;
        int rowsUpdated = sellerRepository.updateActiveStatus(id, statusValue);

        if (rowsUpdated == 0) {
            log.info("Status update had no effect for seller id={}", id);
        } else {
            log.info("Seller id={} status updated to active={}", id, active);
        }

        // Refresh the entity to reflect new status
        seller.setActive(active);
        return mapToDTO(seller);
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE: Soft-delete (is_active = 0)
    // ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void softDeleteSeller(Long id) {
        log.info("Soft-deleting seller id={}", id);

        // Verify seller exists before soft-deleting
        boolean exists = sellerRepository.existsById(id);
        if (!exists) {
            log.info("Soft-delete failed - seller not found with id={}", id);
            throw new ResourceNotFoundException("Seller", id);
        }

        sellerRepository.updateActiveStatus(id, 0);

        log.info("Seller id={} soft-deleted (deactivated) successfully", id);
    }

    // ─────────────────────────────────────────────────────────────
    // Private Mapper Methods
    // Kept private — only used within this service class.
    // Avoids adding a separate MapStruct / ModelMapper dependency.
    // ─────────────────────────────────────────────────────────────

    /**
     * Maps Seller entity → SellerDTO (for API responses).
     */
    private SellerDTO mapToDTO(Seller seller) {
        return new SellerDTO(
                seller.getSellerId(),
                seller.getName(),
                seller.getContactPerson(),
                seller.getPhone(),
                seller.getEmail(),
                seller.getAddress(),
                seller.getGstNumber(),
                seller.isActive(),       // int → boolean conversion
                seller.getCreatedAt()
        );
    }

    /**
     * Maps SellerDTO → Seller entity (for create operations).
     * Does NOT copy sellerId or createdAt — those are set by DB / @PrePersist.
     */
    private Seller mapToEntity(SellerDTO dto) {
        Seller seller = new Seller();
        seller.setName(dto.getName());
        seller.setContactPerson(dto.getContactPerson());
        seller.setPhone(dto.getPhone());
        seller.setEmail(dto.getEmail());
        seller.setAddress(dto.getAddress());
        seller.setGstNumber(dto.getGstNumber());
        // Force new sellers to active irrespective of request payload.
        // @PrePersist also enforces this at entity level as a second guard.
        seller.setActive(true);
        return seller;
    }
}
