package com.mts.mts_purchase_service.controller;

import com.mts.mts_purchase_service.models.ApiResponseDTO;
import com.mts.mts_purchase_service.models.ProductTypeDTO;
import com.mts.mts_purchase_service.service.ProductTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for product type master management.
 */
@RestController
@RequestMapping("/api/product-types")
@CrossOrigin(origins = "*")
@Tag(name = "Product Types", description = "Manage product type lookup values.")
public class ProductTypeController {

    private final ProductTypeService productTypeService;

    /** Injects service dependency for product type APIs. */
    public ProductTypeController(ProductTypeService productTypeService) {
        this.productTypeService = productTypeService;
    }

    /** Returns all product types. */
    @GetMapping
    @Operation(summary = "List product types")
    public ResponseEntity<ApiResponseDTO<List<ProductTypeDTO>>> getAllProductTypes() {
        return ResponseEntity.ok(ApiResponseDTO.success("Product types fetched successfully", productTypeService.getAllProductTypes()));
    }

    /** Returns one product type by id. */
    @GetMapping("/{id}")
    @Operation(summary = "Get product type by id")
    public ResponseEntity<ApiResponseDTO<ProductTypeDTO>> getProductTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success("Product type fetched successfully", productTypeService.getProductTypeById(id)));
    }

    /** Creates a product type. */
    @PostMapping
    @Operation(
            summary = "Create product type",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "CreateProductType",
                                    value = """
                                            {
                                              "typeName": "Edible Oils",
                                              "description": "Oil products and related items"
                                            }
                                            """
                            )
                    )
            )
    )
    public ResponseEntity<ApiResponseDTO<ProductTypeDTO>> createProductType(@Valid @RequestBody ProductTypeDTO productTypeDTO) {
        ProductTypeDTO created = productTypeService.createProductType(productTypeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success("Product type created successfully", created));
    }

    /** Updates a product type by id. */
    @PutMapping("/{id}")
    @Operation(summary = "Update product type")
    public ResponseEntity<ApiResponseDTO<ProductTypeDTO>> updateProductType(
            @PathVariable Long id,
            @Valid @RequestBody ProductTypeDTO productTypeDTO) {
        ProductTypeDTO updated = productTypeService.updateProductType(id, productTypeDTO);
        return ResponseEntity.ok(ApiResponseDTO.success("Product type updated successfully", updated));
    }

    /** Deletes one product type by id. */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product type")
    public ResponseEntity<ApiResponseDTO<Void>> deleteProductType(@PathVariable Long id) {
        productTypeService.deleteProductType(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Product type deleted successfully", null));
    }
}
