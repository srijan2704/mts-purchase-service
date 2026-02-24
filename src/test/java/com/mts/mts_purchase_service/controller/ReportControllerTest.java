package com.mts.mts_purchase_service.controller;

import com.mts.mts_purchase_service.models.DailySummaryDTO;
import com.mts.mts_purchase_service.models.DailyPurchaseTrendPointDTO;
import com.mts.mts_purchase_service.models.DailyPurchaseTrendReportDTO;
import com.mts.mts_purchase_service.models.ProductUnitsBreakdownDTO;
import com.mts.mts_purchase_service.models.ProductUnitsTrendReportDTO;
import com.mts.mts_purchase_service.models.SellerPurchasePointDTO;
import com.mts.mts_purchase_service.models.SellerHistoryReportDTO;
import com.mts.mts_purchase_service.models.TopSellersReportDTO;
import com.mts.mts_purchase_service.models.VariantUnitsBreakdownDTO;
import com.mts.mts_purchase_service.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for Report API behavior.
 */
@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ReportController controller = new ReportController(reportService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getDailySummary_shouldReturn200() throws Exception {
        DailySummaryDTO summary = new DailySummaryDTO();
        summary.setDate(LocalDate.of(2026, 2, 24));
        summary.setTotalOrders(2);
        summary.setTotalSpend(new BigDecimal("350.00"));

        when(reportService.getDailySummary(LocalDate.of(2026, 2, 24))).thenReturn(summary);

        mockMvc.perform(get("/api/reports/daily").param("date", "2026-02-24"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalOrders").value(2));
    }

    @Test
    void getSellerHistory_shouldReturn200() throws Exception {
        SellerHistoryReportDTO report = new SellerHistoryReportDTO();
        report.setSellerId(5L);
        report.setSellerName("Sharma Traders");
        report.setTotalOrders(1);
        report.setTotalSpend(new BigDecimal("500.00"));

        when(reportService.getSellerHistory(5L, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 24)))
                .thenReturn(report);

        mockMvc.perform(get("/api/reports/seller/5")
                        .param("from", "2026-02-01")
                        .param("to", "2026-02-24"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sellerName").value("Sharma Traders"));
    }

    @Test
    void getDailyPurchaseTrend_shouldReturn200() throws Exception {
        DailyPurchaseTrendPointDTO p1 = new DailyPurchaseTrendPointDTO();
        p1.setDate(LocalDate.of(2026, 2, 1));
        p1.setTotalPurchase(new BigDecimal("1200.00"));
        DailyPurchaseTrendPointDTO p2 = new DailyPurchaseTrendPointDTO();
        p2.setDate(LocalDate.of(2026, 2, 2));
        p2.setTotalPurchase(new BigDecimal("0.00"));

        DailyPurchaseTrendReportDTO report = new DailyPurchaseTrendReportDTO();
        report.setFrom(LocalDate.of(2026, 2, 1));
        report.setTo(LocalDate.of(2026, 2, 2));
        report.setPoints(List.of(p1, p2));

        when(reportService.getDailyPurchaseTrend(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 2)))
                .thenReturn(report);

        mockMvc.perform(get("/api/reports/trends/daily-purchase")
                        .param("from", "2026-02-01")
                        .param("to", "2026-02-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.points[0].totalPurchase").value(1200.00));
    }

    @Test
    void getTopProductsByUnits_shouldReturn200() throws Exception {
        VariantUnitsBreakdownDTO variant = new VariantUnitsBreakdownDTO();
        variant.setVariantId(10L);
        variant.setVariantLabel("1L x 16 Box");
        variant.setTotalUnits(new BigDecimal("320.000"));

        ProductUnitsBreakdownDTO product = new ProductUnitsBreakdownDTO();
        product.setProductId(1L);
        product.setProductName("Mangal Sunflower Oil");
        product.setTotalUnits(new BigDecimal("500.000"));
        product.setVariants(List.of(variant));

        ProductUnitsTrendReportDTO report = new ProductUnitsTrendReportDTO();
        report.setFrom(LocalDate.of(2026, 2, 1));
        report.setTo(LocalDate.of(2026, 2, 28));
        report.setProducts(List.of(product));

        when(reportService.getTopProductsByUnits(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28)))
                .thenReturn(report);

        mockMvc.perform(get("/api/reports/trends/top-products")
                        .param("from", "2026-02-01")
                        .param("to", "2026-02-28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.products[0].productName").value("Mangal Sunflower Oil"));
    }

    @Test
    void getTopSellersByPurchase_shouldReturn200() throws Exception {
        SellerPurchasePointDTO seller = new SellerPurchasePointDTO();
        seller.setSellerId(1L);
        seller.setSellerName("Sharma Traders");
        seller.setTotalPurchase(new BigDecimal("125000.50"));

        TopSellersReportDTO report = new TopSellersReportDTO();
        report.setFrom(LocalDate.of(2025, 2, 1));
        report.setTo(LocalDate.of(2026, 1, 31));
        report.setSellers(List.of(seller));

        when(reportService.getTopSellersByPurchase(LocalDate.of(2025, 2, 1), LocalDate.of(2026, 1, 31)))
                .thenReturn(report);

        mockMvc.perform(get("/api/reports/trends/top-sellers")
                        .param("from", "2025-02-01")
                        .param("to", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sellers[0].sellerName").value("Sharma Traders"));
    }
}
