package com.enjoyit.service;

import com.enjoyit.domain.OrderItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderSummaryGenerator {
    /**
     * CO-10: 執行資料聚合 (Group By)
     * 將 OrderItem 按品項名稱加總數量
     */
    public Map<String, Integer> createSummary(List<OrderItem> orderItems) {
        // 使用 Map 來存儲：Key 是品項名稱，Value 是總數量
        Map<String, Integer> summary = new HashMap<>();

        for (OrderItem item : orderItems) {
            String name = item.getItemName();
            int qty = item.getQuantity();

            // 如果名稱已存在就累加，不存在就存入初始數量
            summary.put(name, summary.getOrDefault(name, 0) + qty);
        }

        return summary;
    }
}