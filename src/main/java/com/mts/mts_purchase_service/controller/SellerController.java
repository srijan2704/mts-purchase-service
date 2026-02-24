package com.mts.mts_purchase_service.controller;

import com.mts.mts_purchase_service.models.ApiResponseDTO;
import com.mts.mts_purchase_service.models.SellerDTO;
import com.mts.mts_purchase_service.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Seller management.
 *
 * Base URL : /api/sellers
 *
 * Endpoints:
 *   GET    /api/sellers              - List all active sellers
 *   GET    /api/sellers/all          - List all sellers including inactive
 *   GET    /api/sellers/{id}         - Get seller by ID
 *   GET    /api/sellers/search       - Search sellers by name
 *   POST   /api/sellers              - Create new seller
 *   PUT    /api/sellers/{id}         - Update existing seller
 *   PATCH  /api/sellers/{id}/status  - Activate or deactivate a seller
 *   DELETE /api/sellers/{id}         - Soft-delete seller (sets is_active = 0)
 */
@RestController
@RequestMapping("/api/sellers")
@CrossOrigin(origins = "*") // Allow JS frontend on any local port (e.g. 3000, 5500)
@Tag(name = "Sellers", description = "Manage seller master records.")
public class SellerController {
    
    private static final Logger log = LoggerFactory.getLogger(SellerController.class);

    private final SellerService sellerService;

    // Constructor injection — preferred over @Autowired
    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/sellers
    // Returns only active sellers (is_active = 1)
    // Used by: Purchase Order form's seller dropdown
    // ─────────────────────────────────────────────────────────────
    @GetMapping
    @Operation(summary = "List active sellers", description = "Returns sellers where is_active = 1.")
    public ResponseEntity<ApiResponseDTO<List<SellerDTO>>> getAllActiveSellers() {
        log.info("Application started successfully");
      List<SellerDTO> sellers = sellerService.getAllActiveSellers();
        return ResponseEntity.ok(
            ApiResponseDTO.success("Sellers fetched successfully", sellers)
        );  
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/sellers/all
    // Returns ALL sellers including inactive ones
    // Used by: Admin / seller management screen
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/all")
    @Operation(summary = "List all sellers", description = "Returns both active and inactive sellers.")
    public ResponseEntity<ApiResponseDTO<List<SellerDTO>>> getAllSellers() {
        List<SellerDTO> sellers = sellerService.getAllSellers();
        return ResponseEntity.ok(
            ApiResponseDTO.success("All sellers fetched successfully", sellers)
        );
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/sellers/{id}
    // Returns a single seller by primary key
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    @Operation(summary = "Get seller by id", description = "Returns one seller by primary key.")
    public ResponseEntity<ApiResponseDTO<SellerDTO>> getSellerById(@PathVariable Long id) {
        SellerDTO seller = sellerService.getSellerById(id);
        return ResponseEntity.ok(
            ApiResponseDTO.success("Seller fetched successfully", seller)
        );
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/sellers/search?name=sharma
    // Search sellers by partial name match (case-insensitive)
    // Used by: Search box on seller management screen
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/search")
    @Operation(summary = "Search sellers by name", description = "Case-insensitive partial name search among active sellers.")
    public ResponseEntity<ApiResponseDTO<List<SellerDTO>>> searchSellers(
            @RequestParam String name) {
        List<SellerDTO> sellers = sellerService.searchSellersByName(name);
        return ResponseEntity.ok(
            ApiResponseDTO.success("Search results fetched", sellers)
        );
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/sellers
    // Creates a new seller
    // Body: SellerDTO (name, contactPerson are mandatory)
    // ─────────────────────────────────────────────────────────────
    @PostMapping
    @Operation(
            summary = "Create seller",
            description = "Creates a new seller. Provide only business/contact fields; do not send sellerId, active, or createdAt.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "CreateSeller",
                                    value = """
                                            {
                                              "name": "Sharma Traders",
                                              "contactPerson": "Amit Sharma",
                                              "phone": "9876543210",
                                              "email": "amit.sharma@example.com",
                                              "address": "Main Road, Ranchi",
                                              "gstNumber": "20ABCDE1234F1Z5"
                                            }
                                            """
                            )
                    )
            )
    )
    public ResponseEntity<ApiResponseDTO<SellerDTO>> createSeller(
            @Valid @RequestBody SellerDTO sellerDTO) {
        SellerDTO created = sellerService.createSeller(sellerDTO);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponseDTO.success("Seller created successfully", created));
    }

    // ─────────────────────────────────────────────────────────────
    // PUT /api/sellers/{id}
    // Updates all fields of an existing seller
    // Body: SellerDTO (full update)
    // ─────────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    @Operation(summary = "Update seller", description = "Updates seller fields by id.")
    public ResponseEntity<ApiResponseDTO<SellerDTO>> updateSeller(
            @PathVariable Long id,
            @Valid @RequestBody SellerDTO sellerDTO) {
        SellerDTO updated = sellerService.updateSeller(id, sellerDTO);
        return ResponseEntity.ok(
            ApiResponseDTO.success("Seller updated successfully", updated)
        );
    }

    // ─────────────────────────────────────────────────────────────
    // PATCH /api/sellers/{id}/status?active=false
    // Activate or deactivate a seller without full update
    // Used by: Toggle switch on seller management screen
    // ─────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update seller status", description = "Activates or deactivates seller by id.")
    public ResponseEntity<ApiResponseDTO<SellerDTO>> updateSellerStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        SellerDTO updated = sellerService.updateSellerStatus(id, active);
        String message = active ? "Seller activated successfully" : "Seller deactivated successfully";
        return ResponseEntity.ok(ApiResponseDTO.success(message, updated));
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE /api/sellers/{id}
    // Soft-delete: sets is_active = 0
    // Hard delete is intentionally NOT supported — past purchase
    // orders reference this seller and must remain intact.
    // ─────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete seller", description = "Sets seller is_active flag to 0.")
    public ResponseEntity<ApiResponseDTO<Void>> deleteSeller(@PathVariable Long id) {
        sellerService.softDeleteSeller(id);
        return ResponseEntity.ok(
            ApiResponseDTO.success("Seller deactivated successfully", null)
        );
    }       
}
