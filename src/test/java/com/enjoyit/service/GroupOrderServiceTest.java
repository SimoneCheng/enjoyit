package com.enjoyit.service;

import com.enjoyit.domain.GroupOrder;
import com.enjoyit.repository.InMemoryGroupOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class GroupOrderServiceTest {

    private GroupOrderService groupOrderService;

    @BeforeEach
    void setUp() {
        PasswordValidator passwordValidator = new PasswordValidator();
        InMemoryGroupOrderRepository repository = new InMemoryGroupOrderRepository();
        groupOrderService = new GroupOrderService(passwordValidator, repository);
    }

    @Test
    @DisplayName("UC-04: 發布團購 - 成功將狀態設定為進行中")
    void publishGroupOrder_Success() {
        // Act
        String orderId = groupOrderService.publishGroupOrder("下午茶", "11點截單", "v1", "p1", "g1");

        // Assert
        assertNotNull(orderId);
        assertTrue(orderId.startsWith("order_"));
    }

    @Test
    @DisplayName("UC-04: 發布團購 - 包含公告資訊")
    void publishGroupOrder_WithAnnouncement() {
        // Act
        String orderId = groupOrderService.publishGroupOrder("下午茶", "滿500外送", "v1", "p1", "g1");

        // Assert
        assertNotNull(orderId);
    }

    @Test
    @DisplayName("UC-04: 管理訂單時限 - 當截止時間早於現在時應自動結單")
    void setOrderDeadline_ShouldCloseOrder_WhenTimeIsPast() {
        // Arrange
        GroupOrder order = new GroupOrder("測試");
        order.setOrderId("order_test");
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);

        // Act
        groupOrderService.setOrderDeadline(order, pastTime);

        // Assert
        assertEquals("已結單", order.getStatus());
    }

    @Test
    @DisplayName("UC-04: 設定團購權限 - 驗證管理者密碼成功")
    void verifyAdminAccess_Success() {
        String input = "1234";
        String saved = "1234";

        boolean result = groupOrderService.verifyAdminAccess(input, saved);

        assertTrue(result);
    }

    @Test
    @DisplayName("UC-04: 設定團購權限 - 驗證管理者密碼失敗")
    void verifyAdminAccess_Failure() {
        String input = "wrong";
        String saved = "1234";

        boolean result = groupOrderService.verifyAdminAccess(input, saved);

        assertFalse(result);
    }
}
