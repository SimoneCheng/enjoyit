package com.enjoyit.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class GroupOrderTest {

    @Test
    @DisplayName("測試 CO-07: 發布團購後初始狀態應為進行中")
    void testPublishOrderInitialStatus() {
        // 使用建構子建立物件 (Creator Pattern)
        GroupOrder order = new GroupOrder("迷客夏大安店");

        assertEquals("進行中", order.getStatus());
        assertEquals("迷客夏大安店", order.getOrderInfo());
    }

    @Test
    @DisplayName("測試 CO-08: 設定過去的時間應自動變更狀態為已結單")
    void testSetPastDeadline() {
        GroupOrder order = new GroupOrder("測試過期");
        // 設定一個小時前的時間
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);

        order.setOrderDeadline(pastTime);

        assertEquals("已結單", order.getStatus());
    }

    @Test
    @DisplayName("CO-09: 測試管理者密碼驗證")
    void testAdminPassword() {
        GroupOrder order = new GroupOrder("密碼測試");
        order.setAdminPassword("2026");
        // 改用封裝後的驗證方法測試
        assertTrue(order.isPasswordCorrect("2026"));
        assertFalse(order.isPasswordCorrect("wrong"));
    }

    @Test
    @DisplayName("UC-04: 測試編輯公告功能")
    void testUpdateAnnouncement() {
        GroupOrder order = new GroupOrder("編輯公告測試");
        order.setAnnouncement("舊公告");
        assertEquals("舊公告", order.getAnnouncement());

        // 模擬編輯動作[cite: 2]
        order.updateAnnouncement("新公告");
        assertEquals("新公告", order.getAnnouncement());
    }
}