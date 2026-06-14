package com.enjoyit.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentRequestTest {

    @Test
    void testPaymentReportRequest_GettersAndSetters() {
        // Arrange
        PaymentReportRequest request = new PaymentReportRequest();

        // Act
        request.setParticipantId("device_001");
        request.setMethod("轉帳");
        request.setDetails("末五碼 12345");

        // Assert
        assertEquals("device_001", request.getParticipantId());
        assertEquals("轉帳", request.getMethod());
        assertEquals("末五碼 12345", request.getDetails());
    }

    @Test
    void testPaymentConfirmRequest_GettersAndSetters() {
        // Arrange
        PaymentConfirmRequest request = new PaymentConfirmRequest();

        // Act
        request.setParticipantId("device_002");
        request.setAmount(150);

        // Assert
        assertEquals("device_002", request.getParticipantId());
        assertEquals(150, request.getAmount());
    }
}