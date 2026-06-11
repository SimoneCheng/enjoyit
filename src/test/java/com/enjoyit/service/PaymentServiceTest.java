package com.enjoyit.service;

import com.enjoyit.domain.GroupOrder;
import com.enjoyit.domain.OrderItem;
import com.enjoyit.repository.InMemoryGroupOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentServiceTest {

    private PaymentService paymentService;
    private InMemoryGroupOrderRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryGroupOrderRepository();
        paymentService = new PaymentService(repository);

        GroupOrder order = new GroupOrder("測試團購");
        order.setOrderId("order_123");
        order.setAdminPassword("admin_pwd");
        order.getOrderItems().add(
                new OrderItem("p1", "王小明", "m1", "珍奶", 50, List.of(), 1, 50)
        );
        repository.save(order);
    }

    @Test
    void testGetFinanceSummary_ShouldReturnSummary() {
        List<Map<String, Object>> summary = paymentService.getFinanceSummary("order_123");

        assertNotNull(summary);
        assertEquals(1, summary.size());
        assertEquals("王小明", summary.get(0).get("payerName"));
        assertEquals(50, summary.get(0).get("amountDue"));
        assertEquals("未付款", summary.get(0).get("status"));
    }

    @Test
    void testUpdatePaymentStatus_ShouldUpdateOrder() {
        paymentService.updatePaymentStatus("order_123", "王小明", "已付款", "找50", "admin_pwd");

        List<Map<String, Object>> summary = paymentService.getFinanceSummary("order_123");
        assertEquals("已付款", summary.get(0).get("status"));
        assertEquals("找50", summary.get(0).get("remarks"));
    }

    @Test
    void testUpdatePaymentStatus_WithWrongPassword_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.updatePaymentStatus("order_123", "王小明", "已付款", "", "wrong_pwd")
        );

        assertEquals("密碼錯誤，拒絕修改財務狀態！", exception.getMessage());
    }

    @Test
    void testServiceMethods_OrderNotFound_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> paymentService.getFinanceSummary("non_exist"));
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.updatePaymentStatus("non_exist", "王小明", "已付款", "", "admin_pwd")
        );
    }
}
