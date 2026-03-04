package com.mts.mts_purchase_service.service;

import com.mts.mts_purchase_service.entity.OrderItem;
import com.mts.mts_purchase_service.entity.ProductVariant;
import com.mts.mts_purchase_service.entity.PurchaseOrder;
import com.mts.mts_purchase_service.entity.Seller;
import com.mts.mts_purchase_service.exception.ResourceNotFoundException;
import com.mts.mts_purchase_service.models.OrderItemDTO;
import com.mts.mts_purchase_service.models.PurchaseOrderDTO;
import com.mts.mts_purchase_service.repository.ProductVariantRepository;
import com.mts.mts_purchase_service.repository.PurchaseOrderRepository;
import com.mts.mts_purchase_service.repository.SellerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Handles purchase order workflows and line-item validation.
 */
@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SellerRepository sellerRepository;
    private final ProductVariantRepository productVariantRepository;

    public PurchaseOrderServiceImpl(PurchaseOrderRepository purchaseOrderRepository,
                                    SellerRepository sellerRepository,
                                    ProductVariantRepository productVariantRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.sellerRepository = sellerRepository;
        this.productVariantRepository = productVariantRepository;
    }

    /**
     * Returns orders by single date or date range and optional seller filter.
     */
    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderDTO> getOrders(LocalDate date, LocalDate fromDate, LocalDate toDate, Long sellerId) {
        List<PurchaseOrder> orders;

        boolean rangeProvided = fromDate != null || toDate != null;

        if (date != null && rangeProvided) {
            throw new IllegalArgumentException("Provide either date or fromDate/toDate, not both");
        }

        if (rangeProvided) {
            if (fromDate == null || toDate == null) {
                throw new IllegalArgumentException("Both fromDate and toDate are required for range query");
            }
            if (fromDate.isAfter(toDate)) {
                throw new IllegalArgumentException("fromDate cannot be after toDate");
            }

            if (sellerId == null) {
                orders = purchaseOrderRepository.findByOrderDateBetweenOrderByOrderDateDescCreatedAtDesc(fromDate, toDate);
            } else {
                orders = purchaseOrderRepository.findByOrderDateBetweenAndSellerSellerIdOrderByOrderDateDescCreatedAtDesc(
                        fromDate,
                        toDate,
                        sellerId
                );
            }
        } else {
            LocalDate effectiveDate = date != null ? date : LocalDate.now();
            if (sellerId == null) {
                orders = purchaseOrderRepository.findByOrderDateOrderByCreatedAtDesc(effectiveDate);
            } else {
                orders = purchaseOrderRepository.findByOrderDateAndSellerSellerIdOrderByCreatedAtDesc(effectiveDate, sellerId);
            }
        }

        return orders.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    /**
     * Returns one order detail.
     */
    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderDTO getOrderById(Long orderId) {
        PurchaseOrder order = purchaseOrderRepository.findWithDetailsByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", orderId));
        return mapToDTO(order);
    }

    /**
     * Creates order header with validated items and computed totals.
     */
    @Override
    @Transactional
    public PurchaseOrderDTO createOrder(PurchaseOrderDTO orderDTO) {
        Seller seller = sellerRepository.findActiveById(orderDTO.getSellerId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller", orderDTO.getSellerId()));

        PurchaseOrder order = new PurchaseOrder();
        order.setSeller(seller);
        order.setOrderDate(orderDTO.getOrderDate());
        order.setInvoiceNumber(orderDTO.getInvoiceNumber());
        order.setRemarks(orderDTO.getRemarks());
        order.setStatus(normalizeStatus(orderDTO.getStatus()));

        applyItemsAndRecalculateTotal(order, orderDTO.getItems());

        PurchaseOrder saved = purchaseOrderRepository.save(order);
        return mapToDTO(saved);
    }

    /**
     * Updates header fields and optionally replaces line items.
     */
    @Override
    @Transactional
    public PurchaseOrderDTO updateOrder(Long orderId, PurchaseOrderDTO orderDTO) {
        PurchaseOrder existing = purchaseOrderRepository.findWithDetailsByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", orderId));

        Seller seller = sellerRepository.findActiveById(orderDTO.getSellerId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller", orderDTO.getSellerId()));

        existing.setSeller(seller);
        existing.setOrderDate(orderDTO.getOrderDate());
        existing.setInvoiceNumber(orderDTO.getInvoiceNumber());
        existing.setRemarks(orderDTO.getRemarks());
        existing.setStatus(normalizeStatus(orderDTO.getStatus()));

        // Replace items only when caller sends item payload.
        if (orderDTO.getItems() != null) {
            applyItemsAndRecalculateTotal(existing, orderDTO.getItems());
        }

        PurchaseOrder saved = purchaseOrderRepository.save(existing);
        return mapToDTO(saved);
    }

    /**
     * Confirms a purchase order in one click for UI finalize action.
     */
    @Override
    @Transactional
    public PurchaseOrderDTO confirmOrder(Long orderId) {
        PurchaseOrder order = purchaseOrderRepository.findWithDetailsByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", orderId));

        // Idempotent confirm operation: repeated requests keep status confirmed.
        order.setStatus("CONFIRMED");
        PurchaseOrder saved = purchaseOrderRepository.save(order);
        return mapToDTO(saved);
    }

    /**
     * Deletes order only when status is DRAFT.
     */
    @Override
    @Transactional
    public void deleteDraftOrder(Long orderId) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", orderId));

        if (!"DRAFT".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalStateException("Only DRAFT orders can be deleted");
        }

        purchaseOrderRepository.delete(order);
    }

    /**
     * Replaces item list and recomputes order total from item amounts.
     */
    private void applyItemsAndRecalculateTotal(PurchaseOrder order, List<OrderItemDTO> itemDTOs) {
        List<OrderItemDTO> safeItems = itemDTOs == null ? new ArrayList<>() : itemDTOs;

        order.getItems().clear();

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemDTO dto : safeItems) {
            ProductVariant variant = productVariantRepository.findById(dto.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", dto.getVariantId()));

            if (!variant.isActive()) {
                throw new IllegalStateException("Variant is inactive: " + dto.getVariantId());
            }

            OrderItem item = new OrderItem();
            item.setVariant(variant);
            item.setQuantity(dto.getQuantity());
            item.setRatePerUnit(dto.getRatePerUnit());
            item.setNotes(dto.getNotes());

            BigDecimal lineTotal = dto.getQuantity().multiply(dto.getRatePerUnit()).setScale(2, RoundingMode.HALF_UP);
            item.setLineTotal(lineTotal);
            total = total.add(lineTotal);

            order.addItem(item);
        }

        order.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * Maps order entity to response DTO.
     */
    private PurchaseOrderDTO mapToDTO(PurchaseOrder order) {
        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setSellerId(order.getSeller().getSellerId());
        dto.setSellerName(order.getSeller().getName());
        dto.setOrderDate(order.getOrderDate());
        dto.setInvoiceNumber(order.getInvoiceNumber());
        dto.setRemarks(order.getRemarks());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        List<OrderItemDTO> items = order.getItems().stream().map(this::mapItemToDTO).collect(Collectors.toList());
        dto.setItems(items);
        return dto;
    }

    /**
     * Maps order item entity to response DTO with derived totals/labels.
     */
    private OrderItemDTO mapItemToDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setItemId(item.getItemId());
        dto.setVariantId(item.getVariant().getVariantId());
        dto.setProductName(item.getVariant().getProduct().getProductName());
        dto.setVariantLabel(item.getVariant().getVariantLabel());
        dto.setUnitAbbr(item.getVariant().getUnit().getAbbreviation());
        dto.setPackSize(item.getVariant().getPackSize());
        dto.setPiecesPerPack(item.getVariant().getPiecesPerPack());
        dto.setQuantity(item.getQuantity());
        dto.setRatePerUnit(item.getRatePerUnit());

        BigDecimal lineTotal = item.getLineTotal() != null
                ? item.getLineTotal().setScale(2, RoundingMode.HALF_UP)
                : item.getQuantity().multiply(item.getRatePerUnit()).setScale(2, RoundingMode.HALF_UP);
        dto.setLineTotal(lineTotal);

        BigDecimal totalVolume = item.getQuantity()
                .multiply(item.getVariant().getPackSize())
                .multiply(BigDecimal.valueOf(item.getVariant().getPiecesPerPack()))
                .setScale(3, RoundingMode.HALF_UP);
        dto.setTotalVolume(totalVolume);

        dto.setNotes(item.getNotes());
        return dto;
    }

    /**
     * Validates and normalizes status values.
     */
    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "DRAFT";
        }

        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!"DRAFT".equals(normalized) && !"CONFIRMED".equals(normalized)) {
            throw new IllegalStateException("Invalid order status: " + status);
        }
        return normalized;
    }
}
