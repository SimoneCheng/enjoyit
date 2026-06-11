package com.enjoyit.service;

import com.enjoyit.domain.GroupOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class GroupOrderServiceTest {

    private PasswordValidator passwordValidator;
    private com.enjoyit.repository.GroupOrderRepository groupOrderRepository;
    private GroupOrderService groupOrderService;

    @BeforeEach
    void setUp() {
        passwordValidator = Mockito.mock(PasswordValidator.class);
        groupOrderRepository = new com.enjoyit.repository.InMemoryGroupOrderRepository();
        groupOrderService = new GroupOrderService(passwordValidator, groupOrderRepository);
    }

    @Test
    @DisplayName("UC-04: 發布團購 - 成功將狀態設定為進行中")
    void publishGroupOrder_Success() {
        // Act
        GroupOrder result = groupOrderService.publishGroupOrder("下午茶", "11點截單", "v1", "pwd", "g1");

        // Assert
        assertNotNull(result);
        assertEquals("進行中", result.getStatus());
        assertEquals("v1", result.getVendorId());
    }

    @Test
    @DisplayName("UC-04: 發布團購 - 包含公告資訊")
    void publishGroupOrder_WithAnnouncement() {
        // Act
        GroupOrder result = groupOrderService.publishGroupOrder("下午茶", "滿500外送", "v1", "pwd", "g1");

        // Assert
        assertNotNull(result);
        assertEquals("滿500外送", result.getAnnouncement());
    }

    @Test
    @DisplayName("UC-04: 管理訂單時限 - 當截止時間早於現在時應自動結單")
    void setOrderDeadline_ShouldCloseOrder_WhenTimeIsPast() {
        // Arrange
        GroupOrder order = groupOrderService.publishGroupOrder("測試", null, "v1", "pwd", "g1");
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);

        // Act
        groupOrderService.setOrderDeadline(order, pastTime);

        // Assert
        assertEquals("已結單", order.getStatus());
    }

    @Test
    @DisplayName("檢查特定店家是否有進行中的團購")
    void hasOngoingOrdersByVendor_ShouldReturnTrue_WhenActiveOrderExists() {
        // Arrange
        groupOrderService.publishGroupOrder("測試", null, "vendor1", "pwd", "g1");

        // Act & Assert
        assertTrue(groupOrderService.hasOngoingOrdersByVendor("vendor1"));
        assertFalse(groupOrderService.hasOngoingOrdersByVendor("vendor2"));
    }

    @Test
    @DisplayName("檢查特定店家是否有進行中的團購 - 已結單則不計入")
    void hasOngoingOrdersByVendor_ShouldReturnFalse_WhenOnlyClosedOrdersExist() {
        // Arrange
        GroupOrder order = groupOrderService.publishGroupOrder("測試", null, "vendor1", "pwd", "g1");
        order.setStatus("已結單");
        groupOrderRepository.save(order);

        // Act & Assert
        assertFalse(groupOrderService.hasOngoingOrdersByVendor("vendor1"));
    }
}
