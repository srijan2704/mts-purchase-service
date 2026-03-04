package com.mts.mts_purchase_service.controller;

import com.mts.mts_purchase_service.exception.MtsGlobalExceptionHandler;
import com.mts.mts_purchase_service.exception.ResourceNotFoundException;
import com.mts.mts_purchase_service.models.PurchaseOrderDTO;
import com.mts.mts_purchase_service.service.PurchaseOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for PurchaseOrder API behavior.
 */
@ExtendWith(MockitoExtension.class)
class PurchaseOrderControllerTest {

    @Mock
    private PurchaseOrderService purchaseOrderService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new MtsGlobalExceptionHandler())
                .build();
    }

    @Test
    void createOrder_shouldReturn201() throws Exception {
        PurchaseOrderDTO response = new PurchaseOrderDTO();
        response.setOrderId(99L);
        response.setSellerId(5L);
        response.setSellerName("Sharma Traders");
        response.setOrderDate(LocalDate.of(2026, 2, 24));
        response.setTotalAmount(new BigDecimal("14800.00"));
        response.setItems(new ArrayList<>());

        when(purchaseOrderService.createOrder(org.mockito.ArgumentMatchers.any(PurchaseOrderDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sellerId": 5,
                                  "orderDate": "2026-02-24",
                                  "items": [
                                    {
                                      "variantId": 1,
                                      "quantity": 10,
                                      "ratePerUnit": 1480.00
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value(99));
    }

    @Test
    void getOrderById_shouldReturn404WhenMissing() throws Exception {
        doThrow(new ResourceNotFoundException("PurchaseOrder", 404L))
                .when(purchaseOrderService)
                .getOrderById(404L);

        mockMvc.perform(get("/api/purchase-orders/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void confirmOrder_shouldReturn200() throws Exception {
        PurchaseOrderDTO response = new PurchaseOrderDTO();
        response.setOrderId(99L);
        response.setStatus("CONFIRMED");

        when(purchaseOrderService.confirmOrder(99L)).thenReturn(response);

        mockMvc.perform(patch("/api/purchase-orders/99/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }


    @Test
    void getOrders_shouldSupportDateRangeSingleCall() throws Exception {
        PurchaseOrderDTO response = new PurchaseOrderDTO();
        response.setOrderId(55L);
        response.setOrderDate(LocalDate.of(2026, 3, 14));

        when(purchaseOrderService.getOrders(
                null,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31),
                null
        )).thenReturn(List.of(response));

        mockMvc.perform(get("/api/purchase-orders")
                        .param("fromDate", "2026-03-01")
                        .param("toDate", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].orderId").value(55));
    }
    @Test
    void createOrder_shouldReturn400WhenValidationFails() throws Exception {
        mockMvc.perform(post("/api/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "invoiceNumber": "INV-ONLY"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }
}
