package com.mts.mts_purchase_service.service;

import com.mts.mts_purchase_service.entity.Product;
import com.mts.mts_purchase_service.entity.ProductType;
import com.mts.mts_purchase_service.entity.ProductVariant;
import com.mts.mts_purchase_service.entity.PurchaseOrder;
import com.mts.mts_purchase_service.entity.Seller;
import com.mts.mts_purchase_service.entity.Unit;
import com.mts.mts_purchase_service.exception.ResourceNotFoundException;
import com.mts.mts_purchase_service.models.OrderItemDTO;
import com.mts.mts_purchase_service.models.PurchaseOrderDTO;
import com.mts.mts_purchase_service.repository.ProductVariantRepository;
import com.mts.mts_purchase_service.repository.PurchaseOrderRepository;
import com.mts.mts_purchase_service.repository.SellerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PurchaseOrderServiceImpl business rules.
 */
@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceImplTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private SellerRepository sellerRepository;
    @Mock
    private ProductVariantRepository productVariantRepository;

    private PurchaseOrderServiceImpl purchaseOrderService;

    @BeforeEach
    void setUp() {
        purchaseOrderService = new PurchaseOrderServiceImpl(purchaseOrderRepository, sellerRepository, productVariantRepository);
    }

    @Test
    void createOrder_shouldComputeTotal() {
        Seller seller = new Seller();
        seller.setSellerId(5L);
        seller.setName("Sharma Traders");
        seller.setActive(true);

        ProductType type = new ProductType(3L, "Oils", "desc");
        Product product = new Product();
        product.setProductId(10L);
        product.setProductName("Mangal Oil");
        product.setType(type);

        Unit unit = new Unit(1L, "Litre", "L", "Liquid");

        ProductVariant variant = new ProductVariant();
        variant.setVariantId(1L);
        variant.setProduct(product);
        variant.setUnit(unit);
        variant.setVariantLabel("1L x 16 Box");
        variant.setPackSize(new BigDecimal("1.000"));
        variant.setPiecesPerPack(16);
        variant.setActive(true);

        OrderItemDTO item = new OrderItemDTO();
        item.setVariantId(1L);
        item.setQuantity(new BigDecimal("10"));
        item.setRatePerUnit(new BigDecimal("1480.00"));

        PurchaseOrderDTO request = new PurchaseOrderDTO();
        request.setSellerId(5L);
        request.setOrderDate(LocalDate.of(2026, 2, 24));
        request.setItems(List.of(item));

        when(sellerRepository.findActiveById(5L)).thenReturn(Optional.of(seller));
        when(productVariantRepository.findById(1L)).thenReturn(Optional.of(variant));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseOrderDTO response = purchaseOrderService.createOrder(request);

        assertEquals(new BigDecimal("14800.00"), response.getTotalAmount());
        assertEquals(1, response.getItems().size());
    }

    @Test
    void deleteDraftOrder_shouldFailWhenConfirmed() {
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderId(100L);
        order.setStatus("CONFIRMED");

        when(purchaseOrderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> purchaseOrderService.deleteDraftOrder(100L));
    }

    @Test
    void createOrder_shouldThrowWhenStatusInvalid() {
        Seller seller = new Seller();
        seller.setSellerId(5L);
        seller.setName("Sharma Traders");
        seller.setActive(true);

        ProductType type = new ProductType(3L, "Oils", "desc");
        Product product = new Product();
        product.setProductId(10L);
        product.setProductName("Mangal Oil");
        product.setType(type);

        Unit unit = new Unit(1L, "Litre", "L", "Liquid");
        ProductVariant variant = new ProductVariant();
        variant.setVariantId(1L);
        variant.setProduct(product);
        variant.setUnit(unit);
        variant.setVariantLabel("1L x 16 Box");
        variant.setPackSize(new BigDecimal("1.000"));
        variant.setPiecesPerPack(16);
        variant.setActive(true);

        OrderItemDTO item = new OrderItemDTO();
        item.setVariantId(1L);
        item.setQuantity(new BigDecimal("10"));
        item.setRatePerUnit(new BigDecimal("1480.00"));

        PurchaseOrderDTO request = new PurchaseOrderDTO();
        request.setSellerId(5L);
        request.setOrderDate(LocalDate.of(2026, 2, 24));
        request.setStatus("INVALID_STATUS");
        request.setItems(List.of(item));

        when(sellerRepository.findActiveById(5L)).thenReturn(Optional.of(seller));
        assertThrows(IllegalStateException.class, () -> purchaseOrderService.createOrder(request));
    }

    @Test
    void deleteDraftOrder_shouldDeleteWhenDraft() {
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderId(101L);
        order.setStatus("DRAFT");

        when(purchaseOrderRepository.findById(101L)).thenReturn(Optional.of(order));

        purchaseOrderService.deleteDraftOrder(101L);

        verify(purchaseOrderRepository).delete(order);
    }

    @Test
    void confirmOrder_shouldSetConfirmedStatus() {
        Seller seller = new Seller();
        seller.setSellerId(5L);
        seller.setName("Sharma Traders");

        PurchaseOrder order = new PurchaseOrder();
        order.setOrderId(200L);
        order.setSeller(seller);
        order.setOrderDate(LocalDate.of(2026, 2, 24));
        order.setStatus("DRAFT");
        order.setTotalAmount(new BigDecimal("0.00"));

        when(purchaseOrderRepository.findWithDetailsByOrderId(200L)).thenReturn(Optional.of(order));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseOrderDTO response = purchaseOrderService.confirmOrder(200L);

        assertEquals("CONFIRMED", response.getStatus());
    }

    @Test
    void confirmOrder_shouldThrowWhenOrderMissing() {
        when(purchaseOrderRepository.findWithDetailsByOrderId(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> purchaseOrderService.confirmOrder(999L));
    }
}
