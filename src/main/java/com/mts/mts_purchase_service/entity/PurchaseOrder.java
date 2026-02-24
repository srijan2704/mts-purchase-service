package com.mts.mts_purchase_service.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Purchase order header entity.
 */
@Entity
@Table(name = "PURCHASE_ORDERS")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ORDER_ID")
    private Long orderId;

    // Many orders can belong to one seller.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SELLER_ID", nullable = false)
    private Seller seller;

    @Column(name = "ORDER_DATE", nullable = false)
    private LocalDate orderDate;

    @Column(name = "INVOICE_NUMBER", length = 100)
    private String invoiceNumber;

    @Column(name = "REMARKS", length = 4000)
    private String remarks;

    @Column(name = "TOTAL_AMOUNT", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status = "DRAFT";

    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    // Header controls line-item lifecycle via cascade + orphan removal.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    /** Initializes default timestamps and status before first persist. */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null || this.status.isBlank()) {
            this.status = "DRAFT";
        }
        if (this.totalAmount == null) {
            this.totalAmount = BigDecimal.ZERO;
        }
    }

    /** Updates timestamp before entity update operations. */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /** Returns order id. */
    public Long getOrderId() {
        return orderId;
    }

    /** Assigns order id. */
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    /** Returns seller relation. */
    public Seller getSeller() {
        return seller;
    }

    /** Assigns seller relation. */
    public void setSeller(Seller seller) {
        this.seller = seller;
    }

    /** Returns business order date. */
    public LocalDate getOrderDate() {
        return orderDate;
    }

    /** Assigns business order date. */
    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    /** Returns supplier invoice number. */
    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    /** Assigns supplier invoice number. */
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    /** Returns free-form remarks. */
    public String getRemarks() {
        return remarks;
    }

    /** Assigns free-form remarks. */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /** Returns aggregated order total. */
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    /** Assigns aggregated order total. */
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    /** Returns lifecycle status (DRAFT/CONFIRMED). */
    public String getStatus() {
        return status;
    }

    /** Assigns lifecycle status (DRAFT/CONFIRMED). */
    public void setStatus(String status) {
        this.status = status;
    }

    /** Returns creation timestamp. */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** Assigns creation timestamp. */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /** Returns last update timestamp. */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /** Assigns last update timestamp. */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** Returns line items for this order. */
    public List<OrderItem> getItems() {
        return items;
    }

    /** Assigns full line item list for this order. */
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    /** Adds one line item and synchronizes back-reference. */
    public void addItem(OrderItem item) {
        this.items.add(item);
        item.setOrder(this);
    }
}
