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

    private PasswordValidator passwordValidator; // 被 Mock 的依賴
    private GroupOrderService groupOrderService;

    @BeforeEach
    void setUp() {
        // 統一風格：手動初始化 Service 並注入 Mock 好的 Validator
        passwordValidator = Mockito.mock(PasswordValidator.class);
        groupOrderService = new GroupOrderService(passwordValidator);
    }

    @Test
    @DisplayName("UC-04: 發布團購 - 成功將狀態設定為進行中")
    void publishGroupOrder_Success() {
        // Act
        GroupOrder result = groupOrderService.publishGroupOrder("下午茶", "11點截單");

        // Assert
        assertNotNull(result);
        assertEquals("進行中", result.getStatus());
    }

    @Test
    @DisplayName("UC-04: 發布團購 - 包含公告資訊")
    void publishGroupOrder_WithAnnouncement() {
        // Act
        GroupOrder result = groupOrderService.publishGroupOrder("下午茶", "滿500外送");

        // Assert
        assertNotNull(result);
        assertEquals("滿500外送", result.getAnnouncement());
    }

    @Test
    @DisplayName("UC-04: 管理訂單時限 - 當截止時間早於現在時應自動結單")
    void setOrderDeadline_ShouldCloseOrder_WhenTimeIsPast() {
        // Arrange
        GroupOrder order = new GroupOrder("測試");
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);

        // Act
        groupOrderService.setOrderDeadline(order, pastTime);

        // Assert
        assertEquals("已結單", order.getStatus());
    }

    @Test
    @DisplayName("UC-04: 設定團購權限 - 驗證管理者密碼成功")
    void verifyAdminAccess_Success() {
        // Arrange
        String input = "1234";
        String saved = "1234";
        when(passwordValidator.isValid(input, saved)).thenReturn(true);

        // Act
        boolean result = groupOrderService.verifyAdminAccess(input, saved);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("UC-04: 設定團購權限 - 驗證管理者密碼失敗")
    void verifyAdminAccess_Failure() {
        // Arrange
        String input = "wrong";
        String saved = "1234";
        when(passwordValidator.isValid(input, saved)).thenReturn(false);

        // Act
        boolean result = groupOrderService.verifyAdminAccess(input, saved);

        // Assert
        assertFalse(result);
    }
}
