package com.enjoyit.service;

import com.enjoyit.domain.OrderItem;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderSummaryGenerator {
    /**
     * CO-10: 執行資料聚合 (Group By)
     * 將 OrderItem 按「品項名稱 + 客製化選項」加總數量，確保店家能正確分辨客製化需求
     */
    public Map<String, Integer> createSummary(List<OrderItem> items) {
        return items.stream()
                .collect(Collectors.groupingBy(
                        // 升級：把品項名稱跟客製化選項合併成一個字串當作 Key
                        item -> {
                            String name = item.getItemName();
                            List<String> customs = item.getCustomizations();

                            // 如果沒有客製化選項，就回傳純名稱 (例如："四季春青茶")
                            if (customs == null || customs.isEmpty()) {
                                return name;
                            }
                            // 如果有客製化，就組合成字串 (例如："排骨飯 [加飯(+15), 加蛋(+15)]")
                            return name + " " + customs.toString();
                        },
                        Collectors.summingInt(OrderItem::getQuantity)
                ));
    }

    public Map<String, Object> createDetailedSummary(List<OrderItem> items) {
        Map<String, Integer> aggregatedItems = createSummary(items);
        Map<String, Integer> amountByOrderFor = new LinkedHashMap<>();
        Map<String, List<Map<String, Object>>> submittedByParticipant = new LinkedHashMap<>();

        for (OrderItem item : items) {
            String orderFor = safeName(item.getOrderFor());
            amountByOrderFor.merge(orderFor, item.getOrderTotalPrice(), Integer::sum);

            String participantId = safeName(item.getParticipantId());
            List<Map<String, Object>> participantItems = submittedByParticipant.computeIfAbsent(
                    participantId,
                    ignored -> new ArrayList<>()
            );

            Map<String, Object> record = new LinkedHashMap<>();
            record.put("orderFor", orderFor);
            record.put("itemName", item.getItemName());
            record.put("quantity", item.getQuantity());
            record.put("orderTotalPrice", item.getOrderTotalPrice());
            record.put("customizations", item.getCustomizations());
            participantItems.add(record);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("aggregatedItems", aggregatedItems);
        summary.put("amountByOrderFor", amountByOrderFor);
        summary.put("submittedByParticipant", submittedByParticipant);
        summary.put("totalQuantity", items.stream().mapToInt(OrderItem::getQuantity).sum());
        summary.put("totalAmount", items.stream().mapToInt(OrderItem::getOrderTotalPrice).sum());
        return summary;
    }

    private String safeName(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "未命名";
        }
        return value.trim();
    }
}
