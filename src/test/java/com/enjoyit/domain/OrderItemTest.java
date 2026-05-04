package com.enjoyit.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemTest {

    @Test
    @DisplayName("測試帶參數建構子：應正確儲存所有點餐快照資訊並產生 UUID")
    void testParameterizedConstructor() {
        List<String> customs = List.of("半糖(+0)", "加珍珠(+10)");
        OrderItem item = new OrderItem("user_123", "王小明", "item_01", "珍珠奶茶", 50, customs, 2, 120);

        assertNotNull(item.getItemID(), "應自動產生訂單項目的 UUID");
        assertEquals("user_123", item.getParticipantId());
        assertEquals("王小明", item.getOrderFor());
        assertEquals("item_01", item.getMenuItemId());
        assertEquals("珍珠奶茶", item.getItemName());
        assertEquals(50, item.getUnitPrice());
        assertEquals(2, item.getCustomizations().size());
        assertEquals(2, item.getQuantity());
        assertEquals(120, item.getOrderTotalPrice());
    }

    @Test
    @DisplayName("測試空建構子 (反序列化防護)：應自動產生 UUID 並初始化空的客製化清單")
    void testEmptyConstructor() {
        OrderItem item = new OrderItem();

        assertNotNull(item.getItemID(), "空建構子也必須確保 UUID 被生成");
        assertNotNull(item.getCustomizations(), "客製化清單不應為 null，避免 NullPointerException");
        assertTrue(item.getCustomizations().isEmpty());
    }

    @Test
    @DisplayName("測試屬性修改：Setter 應能正常運作")
    void testSetters() {
        OrderItem item = new OrderItem();
        item.setOrderTotalPrice(150);
        item.setQuantity(3);

        assertEquals(150, item.getOrderTotalPrice());
        assertEquals(3, item.getQuantity());
    }
}