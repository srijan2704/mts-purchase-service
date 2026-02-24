package com.mts.mts_purchase_service.exception;

import com.mts.mts_purchase_service.models.ApiResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for global exception handling behavior.
 */
class MtsGlobalExceptionHandlerTest {

    /**
     * Verifies invalid URL exceptions return 404 with standard API error payload.
     */
    @Test
    void handleNoResourceFound_shouldReturn404WithApiErrorPayload() {
        MtsGlobalExceptionHandler handler = new MtsGlobalExceptionHandler();
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "", "/api/invalid-url");

        ResponseEntity<ApiResponseDTO<Void>> response = handler.handleNoResourceFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("No endpoint found for URL: /api/invalid-url", response.getBody().getMessage());
    }

    /**
     * Verifies malformed/invalid payload errors return 400 with clear message.
     */
    @Test
    void handleUnreadablePayload_shouldReturn400WithFriendlyMessage() {
        MtsGlobalExceptionHandler handler = new MtsGlobalExceptionHandler();
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "JSON parse error",
                new IllegalArgumentException("through reference chain: com.mts.mts_purchase_service.models.SellerDTO[\"active\"]"),
                new MockHttpInputMessage(new byte[0])
        );

        ResponseEntity<ApiResponseDTO<Void>> response = handler.handleUnreadablePayload(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid value for 'active'. Use true/false or remove the field.", response.getBody().getMessage());
    }
}
