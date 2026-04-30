package com.enjoyit.domain;

/**
 * 領域物件：餐點項目 (OrderItem)
 */
public class OrderItem {
    private String itemName;     // 品項名稱
    private int quantity;        // 數量
    private int unitPrice;    // 單價
    private String orderFor;     // 實際訂購者姓名 (支援代點功能)

    public OrderItem(String itemName, int quantity, int unitPrice, String orderFor) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.orderFor = orderFor;
    }

    // Getters
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public int getUnitPrice() { return unitPrice; }
    public String getOrderFor() { return orderFor; }

    // 衍生屬性：計算該項目的小計金額
    public int getSubtotal() {
        return this.unitPrice * this.quantity;
    }
}