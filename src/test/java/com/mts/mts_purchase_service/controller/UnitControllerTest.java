package com.mts.mts_purchase_service.controller;

import com.mts.mts_purchase_service.exception.MtsGlobalExceptionHandler;
import com.mts.mts_purchase_service.exception.ResourceNotFoundException;
import com.mts.mts_purchase_service.models.UnitDTO;
import com.mts.mts_purchase_service.service.UnitService;
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
class UnitControllerTest {

    @Mock
    private UnitService unitService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UnitController controller = new UnitController(unitService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new MtsGlobalExceptionHandler())
                .build();
    }

    @Test
    void createUnit_shouldReturn201() throws Exception {
        UnitDTO response = new UnitDTO(1L, "Piece", "pcs", "Per item unit");

        when(unitService.createUnit(org.mockito.ArgumentMatchers.any(UnitDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/units")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "unitName": "Piece",
                                  "abbreviation": "pcs",
                                  "description": "Per item unit"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.unitName").value("Piece"));
    }

    @Test
    void getUnitById_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Unit", 99L))
                .when(unitService)
                .getUnitById(99L);

        mockMvc.perform(get("/api/units/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
