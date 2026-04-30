package com.enjoyit.service;

import com.enjoyit.domain.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class OrderSummaryGeneratorTest {

    private OrderSummaryGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new OrderSummaryGenerator();
    }

    @Test
    @DisplayName("CO-10: 測試品項聚合統計 - 相同品項應加總數量")
    void testCreateSummaryAggregation() {
        // 準備測試資料：模擬多人點餐
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem("大杯珍珠奶茶", 2, 60, "宇劭"));
        items.add(new OrderItem("大杯珍珠奶茶", 1, 60, "小明"));
        items.add(new OrderItem("紅豆拿鐵", 1, 75, "乃甄"));

        // 執行聚合運算
        Map<String, Integer> summary = generator.createSummary(items);

        // 驗證結果
        assertEquals(2, summary.size(), "應該只有兩種品項");
        assertEquals(3, summary.get("大杯珍珠奶茶"), "珍珠奶茶總數應為 3 (2+1)");
        assertEquals(1, summary.get("紅豆拿鐵"), "紅豆拿鐵總數應為 1");
    }

    @Test
    @DisplayName("CO-10: 測試空訂單處理 - 應回傳空 Map")
    void testCreateSummaryWithEmptyList() {
        List<OrderItem> emptyItems = new ArrayList<>();
        Map<String, Integer> summary = generator.createSummary(emptyItems);

        assertTrue(summary.isEmpty(), "空訂單應產出空的統計表");
    }
}