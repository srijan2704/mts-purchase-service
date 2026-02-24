package com.mts.mts_purchase_service.controller;

import com.mts.mts_purchase_service.exception.MtsGlobalExceptionHandler;
import com.mts.mts_purchase_service.exception.ResourceNotFoundException;
import com.mts.mts_purchase_service.models.ProductVariantDTO;
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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for Variant API behavior.
 */
@ExtendWith(MockitoExtension.class)
class ProductVariantControllerTest {

    @Mock
    private ProductVariantService productVariantService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ProductVariantController controller = new ProductVariantController(productVariantService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new MtsGlobalExceptionHandler())
                .build();
    }

    @Test
    void addVariant_shouldReturn201() throws Exception {
        ProductVariantDTO response = new ProductVariantDTO(1L, 10L, 1L, "Litre", "1L x 16 Box", new BigDecimal("1.000"), 16, null, true);

        when(productVariantService.addVariant(org.mockito.ArgumentMatchers.eq(10L), org.mockito.ArgumentMatchers.any(ProductVariantDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/products/10/variants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "unitId": 1,
                                  "variantLabel": "1L x 16 Box",
                                  "packSize": 1.000,
                                  "piecesPerPack": 16
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.variantLabel").value("1L x 16 Box"));
    }

    @Test
    void deleteVariant_shouldReturn404WhenMissing() throws Exception {
        doThrow(new ResourceNotFoundException("ProductVariant", 55L))
                .when(productVariantService)
                .softDeleteVariant(55L);

        mockMvc.perform(delete("/api/variants/55"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void addVariant_shouldReturn400WhenValidationFails() throws Exception {
        mockMvc.perform(post("/api/products/10/variants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "packSize": 1.000
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }
}
