package com.mts.mts_purchase_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Unit lookup entity (kg, litre, piece, etc.).
 */
@Entity
@Table(name = "UNITS")
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UNIT_ID")
    private Long unitId;

    @Column(name = "UNIT_NAME", nullable = false, length = 50, unique = true)
    private String unitName;

    @Column(name = "ABBREVIATION", nullable = false, length = 10)
    private String abbreviation;

    @Column(name = "DESCRIPTION", length = 200)
    private String description;

    /** Required by JPA. */
    public Unit() {
    }

    /** Convenience constructor used in tests and mapping flows. */
    public Unit(Long unitId, String unitName, String abbreviation, String description) {
        this.unitId = unitId;
        this.unitName = unitName;
        this.abbreviation = abbreviation;
        this.description = description;
    }

    /** Returns primary key id. */
    public Long getUnitId() {
        return unitId;
    }

    /** Assigns primary key id. */
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
