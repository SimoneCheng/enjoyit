package com.enjoyit.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentRecordTest {

    @Test
    void testConstructor_ShouldInitializeWithDefaultValues() {
        // Act
        PaymentRecord record = new PaymentRecord("device_123", 150);

        // Assert
        assertEquals("device_123", record.getParticipantId());
        assertEquals(150, record.getAmountDue());
        assertEquals("未付款", record.getStatus());
        assertEquals("", record.getRemarks());
    }

    @Test
    void testMarkAsPaid_ShouldUpdateStatus() {
        // Arrange
        PaymentRecord record = new PaymentRecord("device_123", 150);

        // Act
        record.markAsPaid();

        // Assert
        assertEquals("已付款", record.getStatus());
    }

    @Test
    void testMarkAsUnpaid_ShouldUpdateStatus() {
        // Arrange
        PaymentRecord record = new PaymentRecord("device_123", 150);
        record.markAsPaid(); // 先設為已付款

        // Act
        record.markAsUnpaid(); // 再設回未付款

        // Assert
        assertEquals("未付款", record.getStatus());
    }
}