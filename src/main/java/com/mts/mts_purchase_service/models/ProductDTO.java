package com.mts.mts_purchase_service.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for product APIs. Includes optional nested variants on create/read.
 */
public class ProductDTO {

    private Long productId;

    @NotBlank(message = "Product name is required")
    @Size(max = 150, message = "Product name must not exceed 150 characters")
    private String productName;

    @NotNull(message = "Type id is required")
    private Long typeId;

    private String typeName;

    @Size(max = 4000, message = "Description must not exceed 4000 characters")
    private String description;

    private Boolean active;
    private LocalDateTime createdAt;

    @Valid
    private List<ProductVariantDTO> variants = new ArrayList<>();

    public ProductDTO() {
    }

    public ProductDTO(Long productId, String productName, Long typeId, String typeName,
                      String description, Boolean active, LocalDateTime createdAt,
                      List<ProductVariantDTO> variants) {
        this.productId = productId;
        this.productName = productName;
        this.typeId = typeId;
        this.typeName = typeName;
        this.description = description;
        this.active = active;
        this.createdAt = createdAt;
        this.variants = variants == null ? new ArrayList<>() : variants;
    }

    /** Returns product primary key. */
    public Long getProductId() {
        return productId;
    }

    /** Assigns product primary key. */
    public void setProductId(Long productId) {
        this.productId = productId;
    }

    /** Returns business-facing product name. */
    public String getProductName() {
        return productName;
    }

    /** Assigns business-facing product name. */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /** Returns lookup type id for this product. */
    public Long getTypeId() {
        return typeId;
    }

    /** Assigns lookup type id for this product. */
    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    /** Returns human-readable type name for response payloads. */
    public String getTypeName() {
        return typeName;
    }

    /** Assigns human-readable type name for response payloads. */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    /** Returns optional product description. */
    public String getDescription() {
        return description;
    }

    /** Assigns optional product description. */
    public void setDescription(String description) {
        this.description = description;
    }

    /** Returns whether product is active. */
    public Boolean isActive() {
        return active;
    }

    /** Assigns active flag for product. */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /** Returns creation timestamp provided by entity lifecycle. */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** Assigns creation timestamp provided by entity lifecycle. */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /** Returns nested variants when requested/created together. */
    public List<ProductVariantDTO> getVariants() {
        return variants;
    }

    /** Assigns nested variants list. */
    public void setVariants(List<ProductVariantDTO> variants) {
        this.variants = variants;
    }
}
