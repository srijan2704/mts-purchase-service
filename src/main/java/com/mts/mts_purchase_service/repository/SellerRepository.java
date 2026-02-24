package com.mts.mts_purchase_service.repository;



import com.mts.mts_purchase_service.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for the Seller entity.
 *
 * JpaRepository<Seller, Long> provides these out of the box:
 *   save(), findById(), findAll(), deleteById(), count(), existsById() ...
 *
 * Custom queries below are added for business-specific lookups.
 */
@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {

    // ─────────────────────────────────────────────────────────────
    // Find all active sellers (is_active = 1)
    // Used by: Purchase Order form's seller dropdown
    // Spring Data derives the query from the method name automatically
    // ─────────────────────────────────────────────────────────────
    List<Seller> findByIsActiveOrderByNameAsc(int isActive);

    // ─────────────────────────────────────────────────────────────
    // Find all sellers regardless of active status, sorted by name
    // Used by: Seller management screen
    // ─────────────────────────────────────────────────────────────
    List<Seller> findAllByOrderByNameAsc();

    // ─────────────────────────────────────────────────────────────
    // Case-insensitive partial name search among active sellers
    // e.g. "sharma" matches "Sharma Traders", "Sharmaji & Co."
    // ─────────────────────────────────────────────────────────────
    @Query("SELECT s FROM Seller s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')) AND s.isActive = 1 ORDER BY s.name ASC")
    List<Seller> searchByNameIgnoreCase(@Param("name") String name);

    // ─────────────────────────────────────────────────────────────
    // Check if a seller with the same name already exists
    // Used by: createSeller() to prevent duplicates
    // ─────────────────────────────────────────────────────────────
    @Query("SELECT COUNT(s) > 0 FROM Seller s WHERE LOWER(s.name) = LOWER(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    // ─────────────────────────────────────────────────────────────
    // Check if another seller (different ID) already has the same name
    // Used by: updateSeller() to prevent name clash during edit
    // ─────────────────────────────────────────────────────────────
    @Query("SELECT COUNT(s) > 0 FROM Seller s WHERE LOWER(s.name) = LOWER(:name) AND s.sellerId <> :id")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("id") Long id);

    // ─────────────────────────────────────────────────────────────
    // Soft-delete: update is_active = 0 directly in DB
    // More efficient than load → set → save for a simple flag update
    // @Modifying + @Transactional required for UPDATE/DELETE queries
    // ─────────────────────────────────────────────────────────────
    @Modifying
    @Query("UPDATE Seller s SET s.isActive = :status WHERE s.sellerId = :id")
    int updateActiveStatus(@Param("id") Long id, @Param("status") int status);

    // ─────────────────────────────────────────────────────────────
    // Find active seller by ID
    // Used when loading a seller specifically for order assignment
    // ─────────────────────────────────────────────────────────────
    @Query("SELECT s FROM Seller s WHERE s.sellerId = :id AND s.isActive = 1")
    Optional<Seller> findActiveById(@Param("id") Long id);
}
