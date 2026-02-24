package com.mts.mts_purchase_service.controller;

import com.mts.mts_purchase_service.models.ApiResponseDTO;
import com.mts.mts_purchase_service.models.ProductDTO;
import com.mts.mts_purchase_service.models.ProductVariantDTO;
import com.mts.mts_purchase_service.service.ProductService;
import com.mts.mts_purchase_service.service.ProductVariantService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Product APIs including filtered listing and product-level CRUD.
 */
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
@Tag(name = "Products", description = "Manage products and list their variants.")
public class ProductController {

    private final ProductService productService;
    private final ProductVariantService productVariantService;

    public ProductController(ProductService productService, ProductVariantService productVariantService) {
        this.productService = productService;
        this.productVariantService = productVariantService;
    }

    /**
     * Lists products with optional filters.
     */
    @GetMapping
    @Operation(summary = "List products", description = "Optional filters: typeId, search, includeInactive, includeVariants.")
    public ResponseEntity<ApiResponseDTO<List<ProductDTO>>> getProducts(
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean includeInactive,
            @RequestParam(defaultValue = "false") boolean includeVariants
    ) {
        List<ProductDTO> products = productService.getProducts(typeId, search, includeInactive, includeVariants);
        return ResponseEntity.ok(ApiResponseDTO.success("Products fetched successfully", products));
    }

    /**
     * Returns one product with active variants.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get product by id")
    public ResponseEntity<ApiResponseDTO<ProductDTO>> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Product fetched successfully", product));
    }

    /**
     * Returns variants of one product.
     */
    @GetMapping("/{id}/variants")
    @Operation(summary = "List variants by product")
    public ResponseEntity<ApiResponseDTO<List<ProductVariantDTO>>> getVariantsByProduct(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        List<ProductVariantDTO> variants = productVariantService.getVariantsByProduct(id, includeInactive);
        return ResponseEntity.ok(ApiResponseDTO.success("Product variants fetched successfully", variants));
    }

    /**
     * Creates product and optional initial variant list.
     */
    @PostMapping
    @Operation(
            summary = "Create product",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "CreateProductWithVariants",
                                    value = """
                                            {
                                              "productName": "Mangal Sunflower Oil",
                                              "typeId": 3,
                                              "description": "Refined sunflower oil",
                                              "variants": [
                                                {
                                                  "unitId": 3,
                                                  "variantLabel": "1L x 16 Box",
                                                  "packSize": 1.000,
                                                  "piecesPerPack": 16
                                                },
                                                {
                                                  "unitId": 3,
                                                  "variantLabel": "5L x 4 Box",
                                                  "packSize": 5.000,
                                                  "piecesPerPack": 4
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
    )
    public ResponseEntity<ApiResponseDTO<ProductDTO>> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        ProductDTO created = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Product created successfully", created));
    }

    /**
     * Updates product-level fields.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update product")
    public ResponseEntity<ApiResponseDTO<ProductDTO>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO
    ) {
        ProductDTO updated = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(ApiResponseDTO.success("Product updated successfully", updated));
    }

    /**
     * Soft-deletes product to retain history-safe references.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete product")
    public ResponseEntity<ApiResponseDTO<Void>> deleteProduct(@PathVariable Long id) {
        productService.softDeleteProduct(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Product deactivated successfully", null));
    }
}
