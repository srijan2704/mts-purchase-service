package com.mts.mts_purchase_service.service;

import com.mts.mts_purchase_service.entity.PurchaseOrder;
import com.mts.mts_purchase_service.entity.Seller;
import com.mts.mts_purchase_service.models.DailyPurchaseTrendReportDTO;
import com.mts.mts_purchase_service.models.DailySummaryDTO;
import com.mts.mts_purchase_service.models.ProductUnitsTrendReportDTO;
import com.mts.mts_purchase_service.models.SellerHistoryReportDTO;
import com.mts.mts_purchase_service.models.TopSellersReportDTO;
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
import static org.mockito.Mockito.when;

/**
 * Unit tests for ReportServiceImpl aggregation behavior.
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private SellerRepository sellerRepository;

    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl(purchaseOrderRepository, sellerRepository);
    }

    @Test
    void getDailySummary_shouldAggregateOrderTotals() {
        LocalDate date = LocalDate.of(2026, 2, 24);

        Seller seller = new Seller();
        seller.setSellerId(5L);
        seller.setName("Sharma Traders");

        PurchaseOrder o1 = new PurchaseOrder();
        o1.setOrderId(1L);
        o1.setSeller(seller);
        o1.setTotalAmount(new BigDecimal("100.00"));

        PurchaseOrder o2 = new PurchaseOrder();
        o2.setOrderId(2L);
        o2.setSeller(seller);
        o2.setTotalAmount(new BigDecimal("250.00"));

        when(purchaseOrderRepository.findByOrderDateAndStatusOrderByCreatedAtDesc(date, "CONFIRMED")).thenReturn(List.of(o1, o2));

        DailySummaryDTO summary = reportService.getDailySummary(date);

        assertEquals(2, summary.getTotalOrders());
        assertEquals(new BigDecimal("350.00"), summary.getTotalSpend());
    }

    @Test
    void getSellerHistory_shouldBuildRangeSummary() {
        LocalDate from = LocalDate.of(2026, 2, 1);
        LocalDate to = LocalDate.of(2026, 2, 24);

        Seller seller = new Seller();
        seller.setSellerId(5L);
        seller.setName("Sharma Traders");

        PurchaseOrder order = new PurchaseOrder();
        order.setOrderId(11L);
        order.setSeller(seller);
        order.setTotalAmount(new BigDecimal("500.00"));
        order.setOrderDate(LocalDate.of(2026, 2, 20));

        when(sellerRepository.findById(5L)).thenReturn(Optional.of(seller));
        when(purchaseOrderRepository.findBySellerSellerIdAndOrderDateBetweenAndStatusOrderByOrderDateDescCreatedAtDesc(5L, from, to, "CONFIRMED"))
                .thenReturn(List.of(order));

        SellerHistoryReportDTO report = reportService.getSellerHistory(5L, from, to);

        assertEquals(1, report.getTotalOrders());
        assertEquals(new BigDecimal("500.00"), report.getTotalSpend());
    }

    @Test
    void getSellerHistory_shouldThrowWhenRangeInvalid() {
        LocalDate from = LocalDate.of(2026, 2, 25);
        LocalDate to = LocalDate.of(2026, 2, 24);

        Seller seller = new Seller();
        seller.setSellerId(5L);
        seller.setName("Sharma Traders");

        when(sellerRepository.findById(5L)).thenReturn(Optional.of(seller));

        assertThrows(IllegalStateException.class, () -> reportService.getSellerHistory(5L, from, to));
    }

    @Test
    void getDailyPurchaseTrend_shouldFillMissingDatesWithZero() {
        LocalDate from = LocalDate.of(2026, 2, 1);
        LocalDate to = LocalDate.of(2026, 2, 3);

        PurchaseOrderRepository.DailyPurchaseTrendProjection row1 = new PurchaseOrderRepository.DailyPurchaseTrendProjection() {
            @Override
            public LocalDate getOrderDate() {
                return LocalDate.of(2026, 2, 1);
            }

            @Override
            public BigDecimal getTotalPurchase() {
                return new BigDecimal("1200.00");
            }
        };

        PurchaseOrderRepository.DailyPurchaseTrendProjection row2 = new PurchaseOrderRepository.DailyPurchaseTrendProjection() {
            @Override
            public LocalDate getOrderDate() {
                return LocalDate.of(2026, 2, 3);
            }

            @Override
            public BigDecimal getTotalPurchase() {
                return new BigDecimal("300.00");
            }
        };

        when(purchaseOrderRepository.findDailyPurchaseTrend(from, to)).thenReturn(List.of(row1, row2));

        DailyPurchaseTrendReportDTO trend = reportService.getDailyPurchaseTrend(from, to);

        assertEquals(3, trend.getPoints().size());
        assertEquals(new BigDecimal("1200.00"), trend.getPoints().get(0).getTotalPurchase());
        assertEquals(new BigDecimal("0.00"), trend.getPoints().get(1).getTotalPurchase());
        assertEquals(new BigDecimal("300.00"), trend.getPoints().get(2).getTotalPurchase());
    }

    @Test
    void getDailyPurchaseTrend_shouldThrowWhenRangeExceedsThreeMonths() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 4, 2);
        assertThrows(IllegalArgumentException.class, () -> reportService.getDailyPurchaseTrend(from, to));
    }

    @Test
    void getTopProductsByUnits_shouldReturnDescendingProductsWithVariants() {
        LocalDate from = LocalDate.of(2026, 2, 1);
        LocalDate to = LocalDate.of(2026, 2, 28);

        PurchaseOrderRepository.ProductUnitsProjection product1 = new PurchaseOrderRepository.ProductUnitsProjection() {
            @Override
            public Long getProductId() {
                return 1L;
            }

            @Override
            public String getProductName() {
                return "Mangal Sunflower Oil";
            }

            @Override
            public BigDecimal getTotalUnits() {
                return new BigDecimal("500.000");
            }
        };

        PurchaseOrderRepository.ProductUnitsProjection product2 = new PurchaseOrderRepository.ProductUnitsProjection() {
            @Override
            public Long getProductId() {
                return 2L;
            }

            @Override
            public String getProductName() {
                return "Fortune Mustard Oil";
            }

            @Override
            public BigDecimal getTotalUnits() {
                return new BigDecimal("300.000");
            }
        };

        PurchaseOrderRepository.VariantUnitsProjection variant1 = new PurchaseOrderRepository.VariantUnitsProjection() {
            @Override
            public Long getProductId() {
                return 1L;
            }

            @Override
            public Long getVariantId() {
                return 10L;
            }

            @Override
            public String getVariantLabel() {
                return "1L x 16 Box";
            }

            @Override
            public BigDecimal getTotalUnits() {
                return new BigDecimal("320.000");
            }
        };

        PurchaseOrderRepository.VariantUnitsProjection variant2 = new PurchaseOrderRepository.VariantUnitsProjection() {
            @Override
            public Long getProductId() {
                return 1L;
            }

            @Override
            public Long getVariantId() {
                return 11L;
            }

            @Override
            public String getVariantLabel() {
                return "5L x 4 Box";
            }

            @Override
            public BigDecimal getTotalUnits() {
                return new BigDecimal("180.000");
            }
        };

        when(purchaseOrderRepository.findProductUnitsByDateRange(from, to)).thenReturn(List.of(product1, product2));
        when(purchaseOrderRepository.findVariantUnitsByDateRange(from, to)).thenReturn(List.of(variant1, variant2));

        ProductUnitsTrendReportDTO report = reportService.getTopProductsByUnits(from, to);

        assertEquals(2, report.getProducts().size());
        assertEquals("Mangal Sunflower Oil", report.getProducts().get(0).getProductName());
        assertEquals(new BigDecimal("500.000"), report.getProducts().get(0).getTotalUnits());
        assertEquals(2, report.getProducts().get(0).getVariants().size());
    }

    @Test
    void getTopProductsByUnits_shouldThrowWhenRangeExceedsOneYear() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 2);
        assertThrows(IllegalArgumentException.class, () -> reportService.getTopProductsByUnits(from, to));
    }

    @Test
    void getTopSellersByPurchase_shouldReturnDescendingSellerTotals() {
        LocalDate from = LocalDate.of(2025, 2, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);

        PurchaseOrderRepository.SellerPurchaseProjection s1 = new PurchaseOrderRepository.SellerPurchaseProjection() {
            @Override
            public Long getSellerId() {
                return 1L;
            }

            @Override
            public String getSellerName() {
                return "Sharma Traders";
            }

            @Override
            public BigDecimal getTotalPurchase() {
                return new BigDecimal("125000.50");
            }
        };

        PurchaseOrderRepository.SellerPurchaseProjection s2 = new PurchaseOrderRepository.SellerPurchaseProjection() {
            @Override
            public Long getSellerId() {
                return 2L;
            }

            @Override
            public String getSellerName() {
                return "Kumar Oil Mills";
            }

            @Override
            public BigDecimal getTotalPurchase() {
                return new BigDecimal("98000.00");
            }
        };

        when(purchaseOrderRepository.findTopSellersByPurchase(from, to)).thenReturn(List.of(s1, s2));

        TopSellersReportDTO report = reportService.getTopSellersByPurchase(from, to);

        assertEquals(2, report.getSellers().size());
        assertEquals("Sharma Traders", report.getSellers().get(0).getSellerName());
        assertEquals(new BigDecimal("125000.50"), report.getSellers().get(0).getTotalPurchase());
    }

    @Test
    void getTopSellersByPurchase_shouldThrowWhenRangeExceedsOneYear() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2025, 2, 1);
        assertThrows(IllegalArgumentException.class, () -> reportService.getTopSellersByPurchase(from, to));
    }
}
