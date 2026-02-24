package com.mts.mts_purchase_service.service;

import com.mts.mts_purchase_service.entity.OrderItem;
import com.mts.mts_purchase_service.entity.PurchaseOrder;
import com.mts.mts_purchase_service.entity.Seller;
import com.mts.mts_purchase_service.models.DailyPurchaseTrendPointDTO;
import com.mts.mts_purchase_service.models.DailyPurchaseTrendReportDTO;
import com.mts.mts_purchase_service.models.ProductUnitsBreakdownDTO;
import com.mts.mts_purchase_service.exception.ResourceNotFoundException;
import com.mts.mts_purchase_service.models.DailySummaryDTO;
import com.mts.mts_purchase_service.models.ProductUnitsTrendReportDTO;
import com.mts.mts_purchase_service.models.PurchaseOrderDTO;
import com.mts.mts_purchase_service.models.SellerPurchasePointDTO;
import com.mts.mts_purchase_service.models.SellerHistoryReportDTO;
import com.mts.mts_purchase_service.models.TopSellersReportDTO;
import com.mts.mts_purchase_service.models.VariantUnitsBreakdownDTO;
import com.mts.mts_purchase_service.repository.PurchaseOrderRepository;
import com.mts.mts_purchase_service.repository.SellerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Aggregates report data from purchase order transactions.
 */
@Service
public class ReportServiceImpl implements ReportService {

    private static final String CONFIRMED_STATUS = "CONFIRMED";
    private static final int DEFAULT_TREND_DAYS = 30;
    private static final int DEFAULT_PRODUCT_WINDOW_DAYS = 30;
    private static final int DEFAULT_SELLER_WINDOW_YEARS = 1;
    private static final int MAX_RANGE_MONTHS = 3;
    private static final int MAX_RANGE_YEARS = 1;

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SellerRepository sellerRepository;

    public ReportServiceImpl(PurchaseOrderRepository purchaseOrderRepository,
                             SellerRepository sellerRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.sellerRepository = sellerRepository;
    }

    /**
     * Builds a daily summary with seller/product/variant spend breakdowns.
     */
    @Override
    @Transactional(readOnly = true)
    public DailySummaryDTO getDailySummary(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        List<PurchaseOrder> orders = purchaseOrderRepository.findByOrderDateAndStatusOrderByCreatedAtDesc(date, CONFIRMED_STATUS);

        DailySummaryDTO summary = new DailySummaryDTO();
        summary.setDate(date);
        summary.setTotalOrders(orders.size());

        BigDecimal totalSpend = BigDecimal.ZERO;
        Map<String, BigDecimal> bySeller = new LinkedHashMap<>();
        Map<String, BigDecimal> byProduct = new LinkedHashMap<>();
        Map<String, BigDecimal> byVariant = new LinkedHashMap<>();

        for (PurchaseOrder order : orders) {
            BigDecimal orderTotal = safe(order.getTotalAmount());
            totalSpend = totalSpend.add(orderTotal);

            bySeller.merge(order.getSeller().getName(), orderTotal, BigDecimal::add);

            for (OrderItem item : order.getItems()) {
                BigDecimal lineTotal = safe(item.getLineTotal());
                String productName = item.getVariant().getProduct().getProductName();
                String variantLabel = item.getVariant().getVariantLabel();

                byProduct.merge(productName, lineTotal, BigDecimal::add);
                byVariant.merge(productName + " :: " + variantLabel, lineTotal, BigDecimal::add);
            }
        }

        summary.setTotalSpend(totalSpend.setScale(2, RoundingMode.HALF_UP));
        summary.setSpendBySeller(scaleMap(bySeller));
        summary.setSpendByProduct(scaleMap(byProduct));
        summary.setSpendByVariant(scaleMap(byVariant));
        return summary;
    }

    /**
     * Builds daily purchase trend report with strict range validation.
     */
    @Override
    @Transactional(readOnly = true)
    public DailyPurchaseTrendReportDTO getDailyPurchaseTrend(LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now();

        // Default range: latest 30 days including today.
        if (from == null && to == null) {
            to = today;
            from = today.minusDays(DEFAULT_TREND_DAYS - 1L);
        } else if (from == null) {
            from = to.minusDays(DEFAULT_TREND_DAYS - 1L);
        } else if (to == null) {
            to = from.plusDays(DEFAULT_TREND_DAYS - 1L);
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' date cannot be after 'to' date");
        }

        if (to.isAfter(from.plusMonths(MAX_RANGE_MONTHS))) {
            throw new IllegalArgumentException("Maximum allowed date range is 3 months");
        }

        List<PurchaseOrderRepository.DailyPurchaseTrendProjection> rows =
                purchaseOrderRepository.findDailyPurchaseTrend(from, to);

        Map<LocalDate, BigDecimal> totalsByDate = new LinkedHashMap<>();
        for (PurchaseOrderRepository.DailyPurchaseTrendProjection row : rows) {
            totalsByDate.put(row.getOrderDate(), safe(row.getTotalPurchase()).setScale(2, RoundingMode.HALF_UP));
        }

        List<DailyPurchaseTrendPointDTO> points = new java.util.ArrayList<>();
        LocalDate cursor = from;
        while (!cursor.isAfter(to)) {
            DailyPurchaseTrendPointDTO point = new DailyPurchaseTrendPointDTO();
            point.setDate(cursor);
            point.setTotalPurchase(totalsByDate.getOrDefault(cursor, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
            points.add(point);
            cursor = cursor.plusDays(1);
        }

        DailyPurchaseTrendReportDTO report = new DailyPurchaseTrendReportDTO();
        report.setFrom(from);
        report.setTo(to);
        report.setPoints(points);
        return report;
    }

    /**
     * Builds product-wise purchased units ranking with variant drill-down.
     */
    @Override
    @Transactional(readOnly = true)
    public ProductUnitsTrendReportDTO getTopProductsByUnits(LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now();

        // Default range: latest 30 days including today.
        if (from == null && to == null) {
            to = today;
            from = today.minusDays(DEFAULT_PRODUCT_WINDOW_DAYS - 1L);
        } else if (from == null) {
            from = to.minusDays(DEFAULT_PRODUCT_WINDOW_DAYS - 1L);
        } else if (to == null) {
            to = from.plusDays(DEFAULT_PRODUCT_WINDOW_DAYS - 1L);
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' date cannot be after 'to' date");
        }

        if (to.isAfter(from.plusYears(MAX_RANGE_YEARS))) {
            throw new IllegalArgumentException("Maximum allowed date range is 1 year");
        }

        List<PurchaseOrderRepository.ProductUnitsProjection> productRows =
                purchaseOrderRepository.findProductUnitsByDateRange(from, to);
        List<PurchaseOrderRepository.VariantUnitsProjection> variantRows =
                purchaseOrderRepository.findVariantUnitsByDateRange(from, to);

        Map<Long, ProductUnitsBreakdownDTO> productMap = new LinkedHashMap<>();
        for (PurchaseOrderRepository.ProductUnitsProjection row : productRows) {
            ProductUnitsBreakdownDTO product = new ProductUnitsBreakdownDTO();
            product.setProductId(row.getProductId());
            product.setProductName(row.getProductName());
            product.setTotalUnits(safe(row.getTotalUnits()).setScale(3, RoundingMode.HALF_UP));
            productMap.put(row.getProductId(), product);
        }

        for (PurchaseOrderRepository.VariantUnitsProjection row : variantRows) {
            ProductUnitsBreakdownDTO parent = productMap.get(row.getProductId());
            if (parent == null) {
                continue;
            }
            VariantUnitsBreakdownDTO variant = new VariantUnitsBreakdownDTO();
            variant.setVariantId(row.getVariantId());
            variant.setVariantLabel(row.getVariantLabel());
            variant.setTotalUnits(safe(row.getTotalUnits()).setScale(3, RoundingMode.HALF_UP));
            parent.getVariants().add(variant);
        }

        ProductUnitsTrendReportDTO report = new ProductUnitsTrendReportDTO();
        report.setFrom(from);
        report.setTo(to);
        report.setProducts(new java.util.ArrayList<>(productMap.values()));
        return report;
    }

    /**
     * Builds seller-wise purchase ranking in descending order.
     */
    @Override
    @Transactional(readOnly = true)
    public TopSellersReportDTO getTopSellersByPurchase(LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now();

        // Default range: latest 1 year including today.
        if (from == null && to == null) {
            to = today;
            from = today.minusYears(DEFAULT_SELLER_WINDOW_YEARS).plusDays(1);
        } else if (from == null) {
            from = to.minusYears(DEFAULT_SELLER_WINDOW_YEARS).plusDays(1);
        } else if (to == null) {
            to = from.plusYears(DEFAULT_SELLER_WINDOW_YEARS).minusDays(1);
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' date cannot be after 'to' date");
        }

        if (to.isAfter(from.plusYears(MAX_RANGE_YEARS))) {
            throw new IllegalArgumentException("Maximum allowed date range is 1 year");
        }

        List<PurchaseOrderRepository.SellerPurchaseProjection> rows =
                purchaseOrderRepository.findTopSellersByPurchase(from, to);

        List<SellerPurchasePointDTO> sellers = new java.util.ArrayList<>();
        for (PurchaseOrderRepository.SellerPurchaseProjection row : rows) {
            SellerPurchasePointDTO point = new SellerPurchasePointDTO();
            point.setSellerId(row.getSellerId());
            point.setSellerName(row.getSellerName());
            point.setTotalPurchase(safe(row.getTotalPurchase()).setScale(2, RoundingMode.HALF_UP));
            sellers.add(point);
        }

        TopSellersReportDTO report = new TopSellersReportDTO();
        report.setFrom(from);
        report.setTo(to);
        report.setSellers(sellers);
        return report;
    }

    /**
     * Builds seller-specific history report for a date range.
     */
    @Override
    @Transactional(readOnly = true)
    public SellerHistoryReportDTO getSellerHistory(Long sellerId, LocalDate from, LocalDate to) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", sellerId));

        if (from == null || to == null) {
            throw new IllegalStateException("Both from and to dates are required");
        }

        if (from.isAfter(to)) {
            throw new IllegalStateException("'from' date cannot be after 'to' date");
        }

        List<PurchaseOrder> orders = purchaseOrderRepository
                .findBySellerSellerIdAndOrderDateBetweenAndStatusOrderByOrderDateDescCreatedAtDesc(
                        sellerId, from, to, CONFIRMED_STATUS
                );

        SellerHistoryReportDTO report = new SellerHistoryReportDTO();
        report.setSellerId(sellerId);
        report.setSellerName(seller.getName());
        report.setFrom(from);
        report.setTo(to);
        report.setTotalOrders(orders.size());

        BigDecimal totalSpend = orders.stream()
                .map(PurchaseOrder::getTotalAmount)
                .map(this::safe)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        report.setTotalSpend(totalSpend);

        // Reuse purchase order service shape for frontend consistency.
        List<PurchaseOrderDTO> orderDTOs = orders.stream().map(this::mapOrderToLightDTO).collect(Collectors.toList());
        report.setOrders(orderDTOs);
        return report;
    }

    /**
     * Maps order to lightweight response for report payload.
     */
    private PurchaseOrderDTO mapOrderToLightDTO(PurchaseOrder order) {
        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setSellerId(order.getSeller().getSellerId());
        dto.setSellerName(order.getSeller().getName());
        dto.setOrderDate(order.getOrderDate());
        dto.setInvoiceNumber(order.getInvoiceNumber());
        dto.setRemarks(order.getRemarks());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(safe(order.getTotalAmount()).setScale(2, RoundingMode.HALF_UP));
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        return dto;
    }

    /**
     * Protects aggregation logic from null monetary values.
     */
    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * Scales all map values to 2 decimals for consistent response formatting.
     */
    private Map<String, BigDecimal> scaleMap(Map<String, BigDecimal> input) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        input.forEach((k, v) -> result.put(k, safe(v).setScale(2, RoundingMode.HALF_UP)));
        return result;
    }
}
