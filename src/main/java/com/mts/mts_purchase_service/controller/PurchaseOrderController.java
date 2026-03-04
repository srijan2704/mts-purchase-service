package com.mts.mts_purchase_service.controller;

import com.mts.mts_purchase_service.models.ApiResponseDTO;
import com.mts.mts_purchase_service.models.PurchaseOrderDTO;
import com.mts.mts_purchase_service.service.PurchaseOrderService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for purchase order workflows.
 */
@RestController
@RequestMapping("/api/purchase-orders")
@CrossOrigin(origins = "*")
@Tag(name = "Purchase Orders", description = "Create and manage daily purchase orders.")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    /**
     * Lists orders by single date or date range with optional seller filter.
     */
    @GetMapping
    @Operation(summary = "List purchase orders", description = "Filters: date or fromDate+toDate, and optional sellerId.")
    public ResponseEntity<ApiResponseDTO<List<PurchaseOrderDTO>>> getOrders(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) Long sellerId
    ) {
        List<PurchaseOrderDTO> orders = purchaseOrderService.getOrders(date, fromDate, toDate, sellerId);
        return ResponseEntity.ok(ApiResponseDTO.success("Purchase orders fetched successfully", orders));
    }

    /**
     * Returns one order detail.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get purchase order by id")
    public ResponseEntity<ApiResponseDTO<PurchaseOrderDTO>> getOrderById(@PathVariable Long id) {
        PurchaseOrderDTO order = purchaseOrderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Purchase order fetched successfully", order));
    }

    /**
     * Creates one order with header + items.
     */
    @PostMapping
    @Operation(
            summary = "Create purchase order",
            description = "Creates a purchase order with header and line items.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "CreatePurchaseOrder",
                                    value = """
                                            {
                                              "sellerId": 1,
                                              "orderDate": "2026-02-23",
                                              "invoiceNumber": "INV-2026-0012",
                                              "remarks": "Morning stock refill",
                                              "items": [
                                                {
                                                  "variantId": 2,
                                                  "quantity": 10,
                                                  "ratePerUnit": 1680.00,
                                                  "notes": "Promo batch"
                                                },
                                                {
                                                  "variantId": 5,
                                                  "quantity": 4,
                                                  "ratePerUnit": 2350.00
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
    )
    public ResponseEntity<ApiResponseDTO<PurchaseOrderDTO>> createOrder(@Valid @RequestBody PurchaseOrderDTO orderDTO) {
        PurchaseOrderDTO created = purchaseOrderService.createOrder(orderDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Purchase order created successfully", created));
    }

    /**
     * Updates one order.
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Update purchase order",
            description = "Updates order header and replaces line items.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "UpdatePurchaseOrder",
                                    value = """
                                            {
                                              "sellerId": 1,
                                              "orderDate": "2026-02-23",
                                              "invoiceNumber": "INV-2026-0012-REV",
                                              "remarks": "Updated quantities",
                                              "items": [
                                                {
                                                  "variantId": 2,
                                                  "quantity": 8,
                                                  "ratePerUnit": 1680.00
                                                },
                                                {
                                                  "variantId": 5,
                                                  "quantity": 6,
                                                  "ratePerUnit": 2325.00
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
    )
    public ResponseEntity<ApiResponseDTO<PurchaseOrderDTO>> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseOrderDTO orderDTO
    ) {
        PurchaseOrderDTO updated = purchaseOrderService.updateOrder(id, orderDTO);
        return ResponseEntity.ok(ApiResponseDTO.success("Purchase order updated successfully", updated));
    }

    /**
     * Confirms one order through single-click UI action.
     */
    @PatchMapping("/{id}/confirm")
    @Operation(summary = "Confirm purchase order", description = "Single-click finalization of an order by setting status to CONFIRMED.")
    public ResponseEntity<ApiResponseDTO<PurchaseOrderDTO>> confirmOrder(@PathVariable Long id) {
        PurchaseOrderDTO confirmed = purchaseOrderService.confirmOrder(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Purchase order confirmed successfully", confirmed));
    }

    /**
     * Deletes one draft order.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete draft purchase order", description = "Deletes an order only when status is DRAFT.")
    public ResponseEntity<ApiResponseDTO<Void>> deleteOrder(@PathVariable Long id) {
        purchaseOrderService.deleteDraftOrder(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Purchase order deleted successfully", null));
    }
}
