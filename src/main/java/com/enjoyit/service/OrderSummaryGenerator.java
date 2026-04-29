package com.enjoyit.service;

import com.enjoyit.domain.OrderItem;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderSummaryGenerator {
    /**
     * CO-10: 執行資料聚合 (Group By)
     * 將 OrderItem 按品項名稱加總數量
     */
    public Map<String, Integer> createSummary(List<OrderItem> items) {
        return items.stream()
                .collect(Collectors.groupingBy(
                        OrderItem::getItemName,
                        Collectors.summingInt(OrderItem::getQuantity)
                ));
    }
}