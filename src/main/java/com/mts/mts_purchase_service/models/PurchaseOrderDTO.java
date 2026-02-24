package com.mts.mts_purchase_service.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Purchase order request/response DTO.
 */
public class PurchaseOrderDTO {

    private Long orderId;

    @NotNull(message = "Seller id is required")
    private Long sellerId;

    private String sellerName;

    @NotNull(message = "Order date is required")
    private LocalDate orderDate;

    @Size(max = 100, message = "Invoice number must not exceed 100 characters")
    private String invoiceNumber;

    @Size(max = 4000, message = "Remarks must not exceed 4000 characters")
    private String remarks;

    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Valid
    private List<OrderItemDTO> items = new ArrayList<>();

    /** Returns order id. */
    public Long getOrderId() {
        return orderId;
    }

    /** Assigns order id. */
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    /** Returns seller id. */
    public Long getSellerId() {
        return sellerId;
    }

    /** Assigns seller id. */
    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    /** Returns seller name for display. */
    public String getSellerName() {
        return sellerName;
    }

    /** Assigns seller name for display. */
    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    /** Returns order date. */
    public LocalDate getOrderDate() {
        return orderDate;
    }

    /** Assigns order date. */
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

    /** Returns remarks text. */
    public String getRemarks() {
        return remarks;
    }

    /** Assigns remarks text. */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /** Returns order total amount. */
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    /** Assigns order total amount. */
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    /** Returns order status. */
    public String getStatus() {
        return status;
    }

    /** Assigns order status. */
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

    /** Returns update timestamp. */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /** Assigns update timestamp. */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** Returns order items. */
    public List<OrderItemDTO> getItems() {
        return items;
    }

    /** Assigns order items. */
    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }
}
