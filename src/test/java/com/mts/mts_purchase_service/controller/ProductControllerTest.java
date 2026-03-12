package com.mts.mts_purchase_service.controller;

import com.mts.mts_purchase_service.exception.MtsGlobalExceptionHandler;
import com.mts.mts_purchase_service.exception.ResourceNotFoundException;
import com.mts.mts_purchase_service.models.ProductDTO;
import com.mts.mts_purchase_service.models.ProductVariantDTO;
import com.mts.mts_purchase_service.service.ProductService;
import com.mts.mts_purchase_service.service.ProductVariantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for Product API behavior.
 */
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private ProductVariantService productVariantService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ProductController controller = new ProductController(productService, productVariantService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new MtsGlobalExceptionHandler())
                .build();
    }

    @Test
    void createProduct_shouldReturn201() throws Exception {
        ProductDTO response = new ProductDTO(1L, "Mangal Oil", 3L, "Edible Oils", "Refined", true, null, new ArrayList<>());

        when(productService.createProduct(org.mockito.ArgumentMatchers.any(ProductDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productName": "Mangal Oil",
                                  "typeId": 3,
                                  "description": "Refined"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productName").value("Mangal Oil"));
    }

    @Test
    void getProductById_shouldReturn404WhenMissing() throws Exception {
        doThrow(new ResourceNotFoundException("Product", 99L))
                .when(productService)
                .getProductById(99L);

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createProduct_shouldReturn400WhenValidationFails() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Missing required fields"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void getVariantsByProduct_shouldReturn200() throws Exception {
        ProductVariantDTO variant = new ProductVariantDTO(
                101L,
                10L,
                2L,
                "Litre",
                "1L x 12",
                new BigDecimal("1.000"),
                12,
                "BAR-100",
                true
        );

        when(productVariantService.getVariantsByProduct(10L, false)).thenReturn(List.of(variant));

        mockMvc.perform(get("/api/products/10/variants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].variantId").value(101))
                .andExpect(jsonPath("$.data[0].variantLabel").value("1L x 12"))
                .andExpect(jsonPath("$.data[0].unitName").value("Litre"));

        verify(productVariantService).getVariantsByProduct(10L, false);
    }
}
