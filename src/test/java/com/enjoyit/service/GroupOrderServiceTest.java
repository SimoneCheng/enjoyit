package com.enjoyit.service;

import com.enjoyit.domain.GroupOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

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
    void publishGroupOrder_Success() {
        // Act
        GroupOrder result = groupOrderService.publishGroupOrder("下午茶", "11點截單");

        // Assert
        assertNotNull(result);
        assertEquals("進行中", result.getStatus()); // 符合 CO-07 Postcondition [cite: 165]
    }

    @Test
    void setOrderDeadline_ShouldCloseOrder_WhenTimeIsPast() {
        // Arrange
        GroupOrder order = new GroupOrder("測試");
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);

        // Act
        groupOrderService.setOrderDeadline(order, pastTime);

        // Assert
        assertEquals("已結單", order.getStatus()); // 符合 CO-08 Postcondition [cite: 167]
    }
}