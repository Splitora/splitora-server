package com.satwik.splitora.exception;

import com.satwik.splitora.persistence.dto.ErrorResponseModel;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void test_handleBadRequestException() {
        BadRequestException ex = new BadRequestException("Bad Request Exception");
        ResponseEntity<ErrorResponseModel> response = handler.handleBadRequestException(ex);
        assertNotNull(response.getBody());
        assertEquals("Bad Request Exception", response.getBody().getMessage());
        assertEquals("BAD_REQUEST", response.getBody().getStatus());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testDataIntegrityViolations() {
        assertDataIntegrityResponse("Data integrity violation", "Data integrity violation");
        assertDataIntegrityResponse("uq_user_email", "Email already exists");
        assertDataIntegrityResponse("uq_user_username", "Username already exists");
    }

    private void assertDataIntegrityResponse(String exceptionMessage, String expectedMessage) {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(exceptionMessage);
        ResponseEntity<ErrorResponseModel> response = handler.handleDataIntegrityViolationException(ex);
        assertNotNull(response.getBody());
        assertEquals(expectedMessage, response.getBody().getMessage());
        assertEquals("BAD_REQUEST", response.getBody().getStatus());
        assertNotNull(response.getBody().getTimestamp());
    }
}
