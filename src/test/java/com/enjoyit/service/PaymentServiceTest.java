package com.enjoyit.service;

import com.enjoyit.domain.GroupOrder;
import com.enjoyit.domain.OrderItem;
import com.enjoyit.repository.GroupOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private GroupOrderRepository groupOrderRepository;

    @InjectMocks
    private PaymentService paymentService;

    private GroupOrder mockOrder;

    @BeforeEach
    void setUp() {
        // 建立測試用的團購訂單與密碼
        mockOrder = new GroupOrder("測試團購");
        mockOrder.setOrderId("order_123");
        mockOrder.setAdminPassword("admin_pwd");

        // 為了讓後續取得對帳或更新狀態不報錯，先塞入一個餐點建立參與者 p1 的資料
        OrderItem item = new OrderItem("p1", "王小明", "m1", "珍奶", 50, List.of(), 1, 50);
        mockOrder.getOrderItems().add(item);
    }

    @Test
    void testGetFinanceSummary_WithCorrectPassword_ShouldReturnSummary() {
        // Arrange
        when(groupOrderRepository.findById("order_123")).thenReturn(Optional.of(mockOrder));

        // Act
        List<Map<String, Object>> summary = paymentService.getFinanceSummary("order_123", "admin_pwd");

        // Assert
        assertNotNull(summary);
        assertEquals(1, summary.size());
        assertEquals("p1", summary.get(0).get("participantId"));
        assertEquals(50, summary.get(0).get("amountDue"));
    }

    @Test
    void testGetFinanceSummary_WithWrongPassword_ShouldThrowException() {
        // Arrange
        when(groupOrderRepository.findById("order_123")).thenReturn(Optional.of(mockOrder));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.getFinanceSummary("order_123", "wrong_pwd");
        });
        assertEquals("密碼錯誤，拒絕存取財務資料！", exception.getMessage());
    }

    @Test
    void testUpdatePaymentStatus_ShouldUpdateAndSave() {
        // Arrange
        when(groupOrderRepository.findById("order_123")).thenReturn(Optional.of(mockOrder));

        // 為了確保對帳紀錄產生，先觸發一次計算
        mockOrder.getFinanceSummary();

        // Act
        paymentService.updatePaymentStatus("order_123", "p1", "已付款", "找50");

        // Assert
        // 驗證狀態是否有改變
        Map<String, Object> p1Status = mockOrder.getSingleParticipantStatus("p1");
        assertEquals("已付款", p1Status.get("status"));

        // 驗證是否有將修改後的訂單存回 Repository
        verify(groupOrderRepository, times(1)).save(mockOrder);
    }

    @Test
    void testGetParticipantStatus_ShouldReturnCorrectStatus() {
        // Arrange
        when(groupOrderRepository.findById("order_123")).thenReturn(Optional.of(mockOrder));

        // Act
        Map<String, Object> status = paymentService.getParticipantStatus("order_123", "p1");

        // Assert
        assertNotNull(status);
        assertEquals(50, status.get("amountDue"));
        assertEquals("未付款", status.get("status")); // 預設狀態
    }

    @Test
    void testServiceMethods_OrderNotFound_ShouldThrowException() {
        // Arrange
        when(groupOrderRepository.findById("non_exist")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.getFinanceSummary("non_exist", "pwd");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.updatePaymentStatus("non_exist", "p1", "已付款", "");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.getParticipantStatus("non_exist", "p1");
        });
    }
}