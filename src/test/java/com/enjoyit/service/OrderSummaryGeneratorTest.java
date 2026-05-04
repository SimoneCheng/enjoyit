package com.enjoyit.service;

import com.enjoyit.domain.OrderItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderSummaryGeneratorTest {

    private final OrderSummaryGenerator generator = new OrderSummaryGenerator();

    @Test
    @DisplayName("測試資料聚合：空清單輸入時應安全回傳空 Map")
    void testCreateSummary_EmptyList() {
        Map<String, Integer> summary = generator.createSummary(Collections.emptyList());
        assertTrue(summary.isEmpty());
    }

    @Test
    @DisplayName("測試資料聚合：無客製化選項時，應直接依照品項名稱合併加總數量")
    void testCreateSummary_NoCustomizations() {
        // 模擬兩份排骨飯，一份雞腿飯
        List<OrderItem> items = Arrays.asList(
                new OrderItem("user1", "A", "item_1", "排骨飯", 100, Collections.emptyList(), 2, 200),
                new OrderItem("user2", "B", "item_1", "排骨飯", 100, Collections.emptyList(), 3, 300),
                new OrderItem("user3", "C", "item_2", "雞腿飯", 120, Collections.emptyList(), 1, 120)
        );

        Map<String, Integer> summary = generator.createSummary(items);

        assertEquals(2, summary.size(), "應該只會分出 2 種餐點");
        assertEquals(5, summary.get("排骨飯"));
        assertEquals(1, summary.get("雞腿飯"));
    }

    @Test
    @DisplayName("測試資料聚合：包含客製化選項時，應將「名稱 + 客製化字串」視為不同品項分開加總")
    void testCreateSummary_WithCustomizations() {
        // 模擬兩杯「半糖, 少冰」的珍奶，以及一杯「全糖, 去冰」的珍奶
        List<OrderItem> items = Arrays.asList(
                new OrderItem("u1", "A", "item_3", "珍珠奶茶", 50, Arrays.asList("半糖", "少冰"), 1, 50),
                new OrderItem("u2", "B", "item_3", "珍珠奶茶", 50, Arrays.asList("半糖", "少冰"), 2, 100),
                new OrderItem("u3", "C", "item_3", "珍珠奶茶", 50, Arrays.asList("全糖", "去冰"), 1, 50)
        );

        Map<String, Integer> summary = generator.createSummary(items);

        assertEquals(2, summary.size(), "客製化不同，應該被拆成 2 種不同的訂單項目");
        // 注意：List.toString() 在 Java 中預設會產生 "[元素1, 元素2]" 的格式
        assertEquals(3, summary.get("珍珠奶茶 [半糖, 少冰]"));
        assertEquals(1, summary.get("珍珠奶茶 [全糖, 去冰]"));
    }

    @Test
    @DisplayName("測試邊界條件：客製化清單為 null 時，應能安全處理等同無客製化")
    void testCreateSummary_NullCustomizations() {
        List<OrderItem> items = Arrays.asList(
                new OrderItem("u1", "A", "item_4", "紅茶", 30, null, 2, 60), // null
                new OrderItem("u2", "B", "item_4", "紅茶", 30, Collections.emptyList(), 1, 30) // empty
        );

        Map<String, Integer> summary = generator.createSummary(items);

        assertEquals(1, summary.size());
        assertEquals(3, summary.get("紅茶"), "null 與空清單都應該被當作純名稱加總");
    }
}
