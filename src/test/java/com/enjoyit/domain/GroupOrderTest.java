package com.enjoyit.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class GroupOrderTest {

    private GroupOrder order;

    @BeforeEach
    void setUp() {
        order = new GroupOrder("測試團購");
    }

    @Test
    void testGetFinanceSummary_ShouldCalculateTotalCorrectly() {
        // Arrange: 加入兩個使用者的餐點
        OrderItem item1 = new OrderItem("p1", "王小明", "m1", "珍奶", 50, List.of("半糖"), 1, 50);
        OrderItem item2 = new OrderItem("p1", "王小明", "m2", "綠茶", 30, List.of(), 2, 60);
        OrderItem item3 = new OrderItem("p2", "陳小美", "m1", "珍奶", 50, List.of(), 1, 50);

        order.getOrderItems().addAll(List.of(item1, item2, item3));

        // Act
        List<Map<String, Object>> summary = order.getFinanceSummary();

        // Assert
        assertEquals(2, summary.size(), "應該只有兩個參與者的帳單");

        // 驗證 p1 的總額 (50 + 60 = 110)
        Map<String, Object> p1Summary = summary.stream().filter(s -> s.get("participantId").equals("p1")).findFirst().get();
        assertEquals(110, p1Summary.get("amountDue"));
        assertEquals("未付款", p1Summary.get("status"));

        // 驗證 p2 的總額 (50)
        Map<String, Object> p2Summary = summary.stream().filter(s -> s.get("participantId").equals("p2")).findFirst().get();
        assertEquals(50, p2Summary.get("amountDue"));
    }

    @Test
    void testGetFinanceSummary_ShouldPreserveStatusAndRemarksWhenRecalculating() {
        // Arrange
        OrderItem item1 = new OrderItem("p1", "王小明", "m1", "珍奶", 50, List.of(), 1, 50);
        order.getOrderItems().add(item1);

        // 第一次計算並手動更改狀態
        order.getFinanceSummary();
        order.updatePaymentStatus("p1", "已付款", "找50元");

        // Act: 使用者加點了一杯飲料，觸發第二次計算
        OrderItem item2 = new OrderItem("p1", "王小明", "m2", "綠茶", 30, List.of(), 1, 30);
        order.getOrderItems().add(item2);
        List<Map<String, Object>> summary = order.getFinanceSummary();

        // Assert: 驗證總額更新，但狀態和備註保留
        Map<String, Object> p1Summary = summary.get(0);
        assertEquals(80, p1Summary.get("amountDue"), "金額應更新為 80");
        assertEquals("已付款", p1Summary.get("status"), "原付款狀態應保留");
        assertEquals("找50元", p1Summary.get("remarks"), "原備註應保留");
    }

    @Test
    void testUpdatePaymentStatus_ShouldThrowExceptionIfParticipantNotFound() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            order.updatePaymentStatus("non_exist_id", "已付款", "");
        });
        assertEquals("找不到該參與者的帳單項目", exception.getMessage());
    }
}