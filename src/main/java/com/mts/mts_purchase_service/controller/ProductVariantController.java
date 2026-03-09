package com.mts.mts_purchase_service.controller;

import com.mts.mts_purchase_service.models.ApiResponseDTO;
import com.mts.mts_purchase_service.models.ProductVariantDTO;
import com.mts.mts_purchase_service.service.ProductVariantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Variant APIs for create/update/soft-delete actions.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Product Variants", description = "Manage variants under each product.")
public class ProductVariantController {

    private final ProductVariantService productVariantService;

    public ProductVariantController(ProductVariantService productVariantService) {
        this.productVariantService = productVariantService;
    }

    /**
     * Returns one variant by id.
     */
    @GetMapping("/variants/{variantId}")
    @Operation(summary = "Get variant by id", description = "Fetches one product variant including product and unit context.")
    public ResponseEntity<ApiResponseDTO<ProductVariantDTO>> getVariantById(@PathVariable Long variantId) {
        ProductVariantDTO variant = productVariantService.getVariantById(variantId);
        return ResponseEntity.ok(ApiResponseDTO.success("Variant fetched successfully", variant));
    }

    /**
     * Adds one variant under a product.
     */
    @PostMapping("/products/{id}/variants")
    @Operation(
            summary = "Add variant to product",
            description = "Creates a new variant under the given product id.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "AddProductVariant",
                                    value = """
                                            {
                                              "unitId": 3,
                                              "variantLabel": "2L x 12 Box",
                                              "packSize": 2.000,
                                              "piecesPerPack": 12,
                                              "barcode": "8901234567890"
                                            }
                                            """
                            )
                    )
            )
    )
    public ResponseEntity<ApiResponseDTO<ProductVariantDTO>> addVariant(
            @PathVariable Long id,
            @Valid @RequestBody ProductVariantDTO variantDTO
    ) {
        ProductVariantDTO created = productVariantService.addVariant(id, variantDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Variant created successfully", created));
    }

    /**
     * Updates one variant.
     */
    @PutMapping("/variants/{variantId}")
    @Operation(
            summary = "Update variant",
            description = "Updates one variant by variant id.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "UpdateVariant",
                                    value = """
                                            {
                                              "unitId": 3,
                                              "variantLabel": "2L x 10 Box",
                                              "packSize": 2.000,
                                              "piecesPerPack": 10,
                                              "barcode": "8901234567890"
                                            }
                                            """
                            )
                    )
            )
    )
    public ResponseEntity<ApiResponseDTO<ProductVariantDTO>> updateVariant(
            @PathVariable Long variantId,
            @Valid @RequestBody ProductVariantDTO variantDTO
    ) {
        ProductVariantDTO updated = productVariantService.updateVariant(variantId, variantDTO);
        return ResponseEntity.ok(ApiResponseDTO.success("Variant updated successfully", updated));
    }

    /**
     * Soft-deletes one variant.
     */
    @DeleteMapping("/variants/{variantId}")
    @Operation(summary = "Soft-delete variant", description = "Marks one variant as inactive.")
    public ResponseEntity<ApiResponseDTO<Void>> deleteVariant(@PathVariable Long variantId) {
        productVariantService.softDeleteVariant(variantId);
        return ResponseEntity.ok(ApiResponseDTO.success("Variant deactivated successfully", null));
    }
}
