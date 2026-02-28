package com.mts.mts_purchase_service;

import com.mts.mts_purchase_service.repository.AppUserRepository;
import com.mts.mts_purchase_service.repository.OrderItemRepository;
import com.mts.mts_purchase_service.repository.ProductRepository;
import com.mts.mts_purchase_service.repository.ProductTypeRepository;
import com.mts.mts_purchase_service.repository.ProductVariantRepository;
import com.mts.mts_purchase_service.repository.PurchaseOrderRepository;
import com.mts.mts_purchase_service.repository.SellerRepository;
import com.mts.mts_purchase_service.repository.UnitRepository;
import com.mts.mts_purchase_service.repository.UserSessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Spring context smoke test that mocks persistence beans and disables JPA auto-config,
 * so tests do not require any real database connectivity.
 */
@SpringBootTest(properties = {
        "PROJ_ENVIRONMENT=dev",
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
                + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration"
})
class MtsPurchaseServiceApplicationTests {

    @MockitoBean
    private AppUserRepository appUserRepository;

    @MockitoBean
    private OrderItemRepository orderItemRepository;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private ProductTypeRepository productTypeRepository;

    @MockitoBean
    private ProductVariantRepository productVariantRepository;

    @MockitoBean
    private PurchaseOrderRepository purchaseOrderRepository;

    @MockitoBean
    private SellerRepository sellerRepository;

    @MockitoBean
    private UnitRepository unitRepository;

    @MockitoBean
    private UserSessionRepository userSessionRepository;

    /**
     * Verifies that Spring can bootstrap the web/service layer without DB access.
     */
    @Test
    void contextLoads() {
        // Context initialization is the assertion for this smoke test.
    }
}
