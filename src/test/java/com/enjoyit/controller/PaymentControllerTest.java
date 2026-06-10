package com.enjoyit.controller;

import com.enjoyit.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    void testGetFinanceSummary_Success() {
        // Arrange
        String orderId = "order_123";
        String password = "admin";
        List<Map<String, Object>> mockSummary = List.of(Map.of("participantId", "p1"));
        when(paymentService.getFinanceSummary(orderId, password)).thenReturn(mockSummary);

        // Act
        ResponseEntity<?> response = paymentController.getFinanceSummary(orderId, password);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockSummary, response.getBody());
    }

    @Test
    void testGetFinanceSummary_WrongPassword() {
        // Arrange
        String orderId = "order_123";
        String password = "wrong_password";
        when(paymentService.getFinanceSummary(orderId, password))
                .thenThrow(new IllegalArgumentException("密碼錯誤，拒絕存取財務資料！"));

        // Act
        ResponseEntity<?> response = paymentController.getFinanceSummary(orderId, password);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("密碼錯誤，拒絕存取財務資料！", response.getBody());
    }

    @Test
    void testUpdatePaymentStatus_Success() {
        // Arrange
        doNothing().when(paymentService).updatePaymentStatus("order_123", "p1", "已付款", "備註");

        // Act
        ResponseEntity<String> response = paymentController.updatePaymentStatus("order_123", "p1", "已付款", "備註");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("狀態與備註更新成功", response.getBody());
    }
}