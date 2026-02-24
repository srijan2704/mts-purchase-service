package com.mts.mts_purchase_service.controller;

import com.mts.mts_purchase_service.exception.MtsGlobalExceptionHandler;
import com.mts.mts_purchase_service.exception.ResourceNotFoundException;
import com.mts.mts_purchase_service.models.ProductTypeDTO;
import com.mts.mts_purchase_service.service.ProductTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductTypeControllerTest {

    @Mock
    private ProductTypeService productTypeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ProductTypeController controller = new ProductTypeController(productTypeService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new MtsGlobalExceptionHandler())
                .build();
    }

    @Test
    void createProductType_shouldReturn201() throws Exception {
        ProductTypeDTO response = new ProductTypeDTO(1L, "Spices", "Spice category");

        when(productTypeService.createProductType(org.mockito.ArgumentMatchers.any(ProductTypeDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/product-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "typeName": "Spices",
                                  "description": "Spice category"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.typeName").value("Spices"));
    }

    @Test
    void getProductTypeById_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("ProductType", 99L))
                .when(productTypeService)
                .getProductTypeById(99L);

        mockMvc.perform(get("/api/product-types/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
