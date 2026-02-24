package com.mts.mts_purchase_service.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Unit request/response DTO.
 */
public class UnitDTO {

    private Long unitId;

    @NotBlank(message = "Unit name is required")
    @Size(max = 50, message = "Unit name must not exceed 50 characters")
    private String unitName;

    @NotBlank(message = "Abbreviation is required")
    @Size(max = 10, message = "Abbreviation must not exceed 10 characters")
    private String abbreviation;

    @Size(max = 200, message = "Description must not exceed 200 characters")
    private String description;

    /** Default constructor for serialization frameworks. */
    public UnitDTO() {
    }

    /** All-fields constructor used in mapping and tests. */
    public UnitDTO(Long unitId, String unitName, String abbreviation, String description) {
        this.unitId = unitId;
        this.unitName = unitName;
        this.abbreviation = abbreviation;
        this.description = description;
    }

    /** Returns unit id. */
    public Long getUnitId() {
        return unitId;
    }

    /** Assigns unit id. */
    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    /** Returns unit display name. */
    public String getUnitName() {
        return unitName;
    }

    /** Assigns unit display name. */
    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    /** Returns short abbreviation. */
    public String getAbbreviation() {
        return abbreviation;
    }

    /** Assigns short abbreviation. */
    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
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
