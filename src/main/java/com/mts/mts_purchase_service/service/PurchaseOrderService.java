package com.mts.mts_purchase_service.service;

import com.mts.mts_purchase_service.models.PurchaseOrderDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Purchase order service contract.
 */
public interface PurchaseOrderService {

    /** Returns orders filtered by single date or date range with optional seller id. */
    List<PurchaseOrderDTO> getOrders(LocalDate date, LocalDate fromDate, LocalDate toDate, Long sellerId);

    /** Returns one order with line details. */
    PurchaseOrderDTO getOrderById(Long orderId);

    /** Creates one order with line items. */
    PurchaseOrderDTO createOrder(PurchaseOrderDTO orderDTO);

    /** Updates header and optional line items for one order. */
    PurchaseOrderDTO updateOrder(Long orderId, PurchaseOrderDTO orderDTO);

    /** Confirms one order with single-click finalization. */
    PurchaseOrderDTO confirmOrder(Long orderId);

    /** Deletes one DRAFT order. */
    void deleteDraftOrder(Long orderId);
}
