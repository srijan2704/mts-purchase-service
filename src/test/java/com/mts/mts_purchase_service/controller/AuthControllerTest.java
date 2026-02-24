package com.mts.mts_purchase_service.controller;

import com.mts.mts_purchase_service.exception.MtsGlobalExceptionHandler;
import com.mts.mts_purchase_service.exception.UnauthorizedException;
import com.mts.mts_purchase_service.models.AuthLoginRequestDTO;
import com.mts.mts_purchase_service.models.AuthSessionResponseDTO;
import com.mts.mts_purchase_service.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for auth APIs.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(authService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new MtsGlobalExceptionHandler())
                .build();
    }

    @Test
    void login_shouldReturn200AndToken() throws Exception {
        AuthSessionResponseDTO response = new AuthSessionResponseDTO();
        response.setUsername("owner");
        response.setToken("abc123");
        response.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        when(authService.login(any(AuthLoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "owner",
                                  "password": "StrongPass123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("abc123"));
    }

    @Test
    void login_shouldReturn401OnInvalidCredential() throws Exception {
        doThrow(new UnauthorizedException("Invalid username or password"))
                .when(authService).login(any(AuthLoginRequestDTO.class));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "owner",
                                  "password": "WrongPass123"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void me_shouldReturn401WhenAuthorizationMissing() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
