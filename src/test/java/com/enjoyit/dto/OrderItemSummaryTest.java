package com.enjoyit.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderItemSummaryTest {

    @Test
    @DisplayName("測試 OrderItemSummary：應正確儲存並回傳統計 Map")
    void testConstructorAndGetter() {
        Map<String, Integer> mockData = new HashMap<>();
        mockData.put("珍珠奶茶", 5);
        mockData.put("排骨飯", 3);

        OrderItemSummary summary = new OrderItemSummary(mockData);

        assertNotNull(summary.getAggregatedData());
        assertEquals(2, summary.getAggregatedData().size());
        assertEquals(5, summary.getAggregatedData().get("珍珠奶茶"));
    }

    @Test
    @DisplayName("測試 getItemTotal：查詢存在的品項時應回傳正確數量")
    void testGetItemTotal_ExistingItem() {
        Map<String, Integer> mockData = new HashMap<>();
        mockData.put("雞腿飯", 10);
        OrderItemSummary summary = new OrderItemSummary(mockData);

        assertEquals(10, summary.getItemTotal("雞腿飯"));
    }

    @Test
    @DisplayName("測試 getItemTotal：查詢不存在的品項時應安全回傳 0 (使用 getOrDefault 機制)")
    void testGetItemTotal_NonExistingItem() {
        Map<String, Integer> mockData = new HashMap<>();
        mockData.put("雞腿飯", 10);
        OrderItemSummary summary = new OrderItemSummary(mockData);

        assertEquals(0, summary.getItemTotal("從沒點過的神秘餐點"), "找不到的餐點應該回傳 0");
    }
}