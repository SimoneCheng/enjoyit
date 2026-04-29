package com.enjoyit.dto;

import java.util.Map;

/**
 * DTO：訂單項目彙整結果
 * 用於封裝 CO-10 執行後的資料聚合結果
 */
public class OrderItemSummary {

    // 儲存聚合後的數據：品項名稱 -> 總數量
    private Map<String, Integer> aggregatedData;

    /**
     * 建構子
     * @param aggregatedData 傳入由 Generator 計算出的 Map
     */
    public OrderItemSummary(Map<String, Integer> aggregatedData) {
        this.aggregatedData = aggregatedData;
    }

    // Getter
    public Map<String, Integer> getAggregatedData() {
        return aggregatedData;
    }

    // Setter
    public void setAggregatedData(Map<String, Integer> aggregatedData) {
        this.aggregatedData = aggregatedData;
    }

    /**
     * 輔助方法：獲取特定品項的總數
     */
    public int getItemTotal(String itemName) {
        return aggregatedData.getOrDefault(itemName, 0);
    }
}