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
        OrderItem item1 = new OrderItem("p1", "王小明", "m1", "珍奶", 50, List.of("半糖"), 1, 50);
        OrderItem item2 = new OrderItem("p1", "王小明", "m2", "綠茶", 30, List.of(), 2, 60);
        OrderItem item3 = new OrderItem("p2", "陳小美", "m1", "珍奶", 50, List.of(), 1, 50);

        order.getOrderItems().addAll(List.of(item1, item2, item3));

        List<Map<String, Object>> summary = order.getFinanceSummary();

        assertEquals(2, summary.size(), "應該只有兩個訂購人的帳單");
        Map<String, Object> p1Summary = summary.stream()
                .filter(s -> s.get("payerName").equals("王小明"))
                .findFirst()
                .orElseThrow();
        assertEquals(110, p1Summary.get("amountDue"));
        assertEquals("未付款", p1Summary.get("status"));

        Map<String, Object> p2Summary = summary.stream()
                .filter(s -> s.get("payerName").equals("陳小美"))
                .findFirst()
                .orElseThrow();
        assertEquals(50, p2Summary.get("amountDue"));
    }

    @Test
    void testGetFinanceSummary_ShouldPreserveStatusAndRemarksWhenRecalculating() {
        // Arrange
        OrderItem item1 = new OrderItem("p1", "王小明", "m1", "珍奶", 50, List.of(), 1, 50);
        order.getOrderItems().add(item1);

        order.getFinanceSummary();
        order.updatePaymentStatus("王小明", "已付款", "找50元");

        OrderItem item2 = new OrderItem("p1", "王小明", "m2", "綠茶", 30, List.of(), 1, 30);
        order.getOrderItems().add(item2);
        List<Map<String, Object>> summary = order.getFinanceSummary();

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
        assertEquals("找不到該訂購人的帳單項目", exception.getMessage());
    }
}
