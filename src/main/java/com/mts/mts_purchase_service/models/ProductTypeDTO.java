package com.mts.mts_purchase_service.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Product type request/response DTO.
 */
public class ProductTypeDTO {

    private Long typeId;

    @NotBlank(message = "Type name is required")
    @Size(max = 100, message = "Type name must not exceed 100 characters")
    private String typeName;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    /** Default constructor for serialization frameworks. */
    public ProductTypeDTO() {
    }

    /** All-fields constructor used in mapping and tests. */
    public ProductTypeDTO(Long typeId, String typeName, String description) {
        this.typeId = typeId;
        this.typeName = typeName;
        this.description = description;
    }

    /** Returns type id. */
    public Long getTypeId() {
        return typeId;
    }

    /** Assigns type id. */
    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    /** Returns type display name. */
    public String getTypeName() {
        return typeName;
    }

    /** Assigns type display name. */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    /** Returns optional description text. */
    public String getDescription() {
        return description;
    }

    /** Assigns optional description text. */
    public void setDescription(String description) {
        this.description = description;
    }
}
