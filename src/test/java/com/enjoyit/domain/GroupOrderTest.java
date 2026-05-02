package com.enjoyit.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class GroupOrderTest {

    @Test
    @DisplayName("測試建立 GroupOrder 時，初始狀態應為進行中")
    void testInitialStatus() {
        GroupOrder order = new GroupOrder("測試團購名稱");

        assertEquals("進行中", order.getStatus());
        assertEquals("測試團購名稱", order.getOrderInfo());
    }

    @Test
    @DisplayName("測試設定未來的截止時間，狀態應維持進行中")
    void testSetFutureDeadline() {
        GroupOrder order = new GroupOrder("測試團購");
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1); // 明天

        order.setOrderDeadline(futureTime);

        assertEquals(futureTime, order.getDeadline());
        assertEquals("進行中", order.getStatus());
    }

    @Test
    @DisplayName("測試設定過去的截止時間，狀態應自動變更為已結單")
    void testSetPastDeadline() {
        GroupOrder order = new GroupOrder("測試團購");
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1); // 一小時前

        order.setOrderDeadline(pastTime);

        assertEquals(pastTime, order.getDeadline());
        assertEquals("已結單", order.getStatus());
    }
}