package com.enjoyit.service;

import com.enjoyit.domain.GroupOrder;
import com.enjoyit.repository.InMemoryGroupOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class GroupOrderServiceTest {

    private InMemoryGroupOrderRepository groupOrderRepository;
    private GroupOrderService groupOrderService;

    @BeforeEach
    void setUp() {
        PasswordValidator passwordValidator = new PasswordValidator();
        groupOrderRepository = new InMemoryGroupOrderRepository();
        groupOrderService = new GroupOrderService(passwordValidator, groupOrderRepository);
    }

    @Test
    @DisplayName("UC-04: 發布團購 - 成功將狀態設定為進行中")
    void publishGroupOrder_Success() {
        String orderId = groupOrderService.publishGroupOrder("下午茶", "11點截單", "v1", "pwd", "g1");
        GroupOrder result = groupOrderService.getOrderById(orderId).orElseThrow();

        assertNotNull(result);
        assertEquals("進行中", result.getStatus());
        assertEquals("v1", result.getVendorId());
        assertTrue(orderId.startsWith("order_"));
    }

    @Test
    @DisplayName("UC-04: 發布團購 - 包含公告資訊")
    void publishGroupOrder_WithAnnouncement() {
        String orderId = groupOrderService.publishGroupOrder("下午茶", "滿500外送", "v1", "p1", "g1");
        GroupOrder result = groupOrderService.getOrderById(orderId).orElseThrow();

        assertEquals("滿500外送", result.getAnnouncement());
    }

    @Test
    @DisplayName("UC-04: 管理訂單時限 - 當截止時間早於現在時應自動結單")
    void setOrderDeadline_ShouldCloseOrder_WhenTimeIsPast() {
        String orderId = groupOrderService.publishGroupOrder("測試", null, "v1", "pwd", "g1");
        GroupOrder order = groupOrderService.getOrderById(orderId).orElseThrow();
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);

        groupOrderService.setOrderDeadline(order, pastTime);

        assertEquals("已結單", order.getStatus());
    }

    @Test
    @DisplayName("檢查特定店家是否有進行中的團購")
    void hasOngoingOrdersByVendor_ShouldReturnTrue_WhenActiveOrderExists() {
        groupOrderService.publishGroupOrder("測試", null, "vendor1", "pwd", "g1");

        assertTrue(groupOrderService.hasOngoingOrdersByVendor("vendor1"));
        assertFalse(groupOrderService.hasOngoingOrdersByVendor("vendor2"));
    }

    @Test
    @DisplayName("檢查特定店家是否有進行中的團購 - 已結單則不計入")
    void hasOngoingOrdersByVendor_ShouldReturnFalse_WhenOnlyClosedOrdersExist() {
        String orderId = groupOrderService.publishGroupOrder("測試", null, "vendor1", "pwd", "g1");
        GroupOrder order = groupOrderService.getOrderById(orderId).orElseThrow();
        order.setStatus("已結單");
        groupOrderRepository.save(order);

        assertFalse(groupOrderService.hasOngoingOrdersByVendor("vendor1"));
    }

    @Test
    @DisplayName("UC-04: 設定團購權限 - 驗證管理者密碼成功")
    void verifyAdminAccess_Success() {
        boolean result = groupOrderService.verifyAdminAccess("1234", "1234");

        assertTrue(result);
    }

    @Test
    @DisplayName("UC-04: 設定團購權限 - 驗證管理者密碼失敗")
    void verifyAdminAccess_Failure() {
        boolean result = groupOrderService.verifyAdminAccess("wrong", "1234");

        assertFalse(result);
    }
}
