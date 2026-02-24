package com.mts.mts_purchase_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Formula;

import java.math.BigDecimal;

/**
 * Purchase order line-item entity.
 */
@Entity
@Table(name = "ORDER_ITEMS")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ITEM_ID")
    private Long itemId;

    // Many items belong to one order header.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_ID", nullable = false)
    private PurchaseOrder order;

    // Each line references one exact product variant SKU.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VARIANT_ID", nullable = false)
    private ProductVariant variant;

    @Column(name = "QUANTITY", nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;

    @Column(name = "RATE_PER_UNIT", nullable = false, precision = 12, scale = 2)
    private BigDecimal ratePerUnit;

    // Mirrors Oracle virtual column expression for read purposes.
    @Formula("(QUANTITY * RATE_PER_UNIT)")
    private BigDecimal lineTotal;

    @Column(name = "NOTES", length = 255)
    private String notes;

    /** Returns line item id. */
    public Long getItemId() {
        return itemId;
    }

    /** Assigns line item id. */
    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    /** Returns parent order relation. */
    public PurchaseOrder getOrder() {
        return order;
    }

    /** Assigns parent order relation. */
    public void setOrder(PurchaseOrder order) {
        this.order = order;
    }

    /** Returns selected variant relation. */
    public ProductVariant getVariant() {
        return variant;
    }

    /** Assigns selected variant relation. */
    public void setVariant(ProductVariant variant) {
        this.variant = variant;
    }

    /** Returns quantity in pack units. */
    public BigDecimal getQuantity() {
        return quantity;
    }

    /** Assigns quantity in pack units. */
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    /** Returns rate per pack unit. */
    public BigDecimal getRatePerUnit() {
        return ratePerUnit;
    }

    /** Assigns rate per pack unit. */
    public void setRatePerUnit(BigDecimal ratePerUnit) {
        this.ratePerUnit = ratePerUnit;
    }

    /** Returns computed line total. */
    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    /** Assigns computed line total (mostly for tests/mapping fallback). */
    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    /** Returns free-form line notes. */
    public String getNotes() {
        return notes;
    }

    /** Assigns free-form line notes. */
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
