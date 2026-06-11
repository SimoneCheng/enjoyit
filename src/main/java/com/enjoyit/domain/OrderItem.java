package com.enjoyit.domain;

import java.util.List;
import java.util.UUID;

public class OrderItem {
    private String itemID; // 訂單項目的唯一 ID

    // --- 訂購者資訊 (UC-05 需求) ---
    private String participantId; // 實際操作系統點餐的人 (帳號/識別碼)
    private String orderFor;      // 代訂對象的姓名 (如果是自己吃，就填自己的名字)

    // --- 關聯至 UC-03 菜單系統 (使用 Snapshot 快照概念保留歷史紀錄) ---
    private String menuItemId;    // 對應菜單上的 MenuItem ID (方便之後互相對照)
    private String itemName;      // 當下點餐時的品項名稱
    private int unitPrice;        // 當下點餐時的品項基礎單價

    // 儲存客製化選項 (例如：["半糖(+0)", "加珍珠(+10)"])
    // 實作上可以存選項的名稱或 ID，這裡為了報表易讀性，存格式化的字串或 DTO 都可以
    private List<String> customizations;

    private int quantity;         // 數量
    private int orderTotalPrice;  // 該品項的小計 ( (基礎單價 + 客製化加價) * 數量 )
    // Constructor
    public OrderItem(String participantId, String orderFor, String menuItemId, String itemName, int unitPrice, List<String> customizations, int quantity, int orderTotalPrice) {
        this.itemID = UUID.randomUUID().toString();
        this.participantId = participantId;
        this.orderFor = orderFor;
        this.menuItemId = menuItemId;
        this.itemName = itemName;
        this.unitPrice = unitPrice;
        this.customizations = customizations;
        this.quantity = quantity;
        this.orderTotalPrice = orderTotalPrice;
    }

    // Spring Boot (Jackson) 將 JSON 轉換成物件時必需的空建構子
    public OrderItem() {
        this.itemID = java.util.UUID.randomUUID().toString();
        // 確保串列不會是 null，避免後續發生 NullPointerException
        this.customizations = new java.util.ArrayList<>();
    }

    // --- Getters and Setters ---
    public String getItemID() { return itemID; }

    public String getParticipantId() { return participantId; }
    public void setParticipantId(String participantId) { this.participantId = participantId; }

    public String getOrderFor() { return orderFor; }
    public void setOrderFor(String orderFor) { this.orderFor = orderFor; }

    public String getMenuItemId() { return menuItemId; }
    public void setMenuItemId(String menuItemId) { this.menuItemId = menuItemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public int getUnitPrice() { return unitPrice; }
    public void setUnitPrice(int unitPrice) { this.unitPrice = unitPrice; }

    public List<String> getCustomizations() { return customizations; }
    public void setCustomizations(List<String> customizations) { this.customizations = customizations; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getOrderTotalPrice() { return orderTotalPrice; }
    public void setOrderTotalPrice(int orderTotalPrice) { this.orderTotalPrice = orderTotalPrice; }
}