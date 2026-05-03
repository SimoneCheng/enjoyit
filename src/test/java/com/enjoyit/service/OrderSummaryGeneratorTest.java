package com.enjoyit.service;

import com.enjoyit.domain.OrderItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderSummaryGeneratorTest {

    private final OrderSummaryGenerator generator = new OrderSummaryGenerator();

    @Test
    @DisplayName("UC-04: 輸出訂單明細 - 成功聚合相同品項與客製化選項")
    void createSummary_AggregatesSameItems() {
        // Arrange
        OrderItem item1 = new OrderItem();
        item1.setItemName("排骨飯");
        item1.setCustomizations(Arrays.asList("加飯(+15)"));
        item1.setQuantity(2);

        OrderItem item2 = new OrderItem();
        item2.setItemName("排骨飯");
        item2.setCustomizations(Arrays.asList("加飯(+15)"));
        item2.setQuantity(3);

        List<OrderItem> items = Arrays.asList(item1, item2);

        // Act
        Map<String, Integer> summary = generator.createSummary(items);

        // Assert
        assertEquals(1, summary.size());
        String key = "排骨飯 [加飯(+15)]";
        assertEquals(5, summary.get(key));
    }

    @Test
    @DisplayName("UC-04: 輸出訂單明細 - 分開計算不同客製化選項的同品項")
    void createSummary_SeparatesDifferentCustomizations() {
        // Arrange
        OrderItem item1 = new OrderItem();
        item1.setItemName("四季春");
        item1.setCustomizations(Arrays.asList("去冰", "半糖"));
        item1.setQuantity(1);

        OrderItem item2 = new OrderItem();
        item2.setItemName("四季春");
        item2.setCustomizations(Arrays.asList("去冰", "微糖"));
        item2.setQuantity(1);

        List<OrderItem> items = Arrays.asList(item1, item2);

        // Act
        Map<String, Integer> summary = generator.createSummary(items);

        // Assert
        assertEquals(2, summary.size());
        assertEquals(1, summary.get("四季春 [去冰, 半糖]"));
        assertEquals(1, summary.get("四季春 [去冰, 微糖]"));
    }

    @Test
    @DisplayName("UC-04: 輸出訂單明細 - 處理無客製化選項的品項")
    void createSummary_HandlesNoCustomizations() {
        // Arrange
        OrderItem item = new OrderItem();
        item.setItemName("雞腿飯");
        item.setCustomizations(Collections.emptyList());
        item.setQuantity(10);

        List<OrderItem> items = Collections.singletonList(item);

        // Act
        Map<String, Integer> summary = generator.createSummary(items);

        // Assert
        assertEquals(10, summary.get("雞腿飯"));
    }
}
