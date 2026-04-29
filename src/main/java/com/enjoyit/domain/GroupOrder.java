package com.enjoyit.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GroupOrder {
    private String status;              // 團購狀態 (進行中/已結單)
    private LocalDateTime deadline;     // 截止時間
    private String adminPassword;      // 管理者密碼
    private String announcement;       // 公告內容
    private List<OrderItem> orderItems = new ArrayList<>();
    private String orderId;            // GroupOrder 唯一識別碼
    private String orderInfo;

    public GroupOrder(String info) {
        this.orderInfo = info; // 確保名稱有存進去
        this.status = "進行中";  // 設定初始狀態
        // 生成管理者密碼，對應 CO-09
        this.adminPassword = "pwd" + String.valueOf(System.currentTimeMillis()).substring(8);

        // 先預設訂單內有這三項商品
        this.orderItems.add(new OrderItem("大杯珍珠奶茶",1, 60, "預設"));
        this.orderItems.add(new OrderItem("紅茶拿鐵", 1, 70, "預設"));
        this.orderItems.add(new OrderItem("四季春青茶", 1, 35, "預設"));
    }

    /**
     * 實作 CO-08 的領域邏輯
     * 設定截止時間並根據目前時間判斷是否自動結單
     */
    public void setOrderDeadline(LocalDateTime newTime) {
        this.deadline = newTime;
        if (newTime.isBefore(LocalDateTime.now())) {
            this.status = "已結單";
        }
    }

    public void setAnnouncement(String announcement) {
        this.announcement = announcement;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setOrderInfo(String orderInfo) {
        this.orderInfo = orderInfo;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    // Getters 供 Controller 或聚合器使用
    public String getStatus() { return status; }
    public String getAdminPassword() { return adminPassword; }
    public List<OrderItem> getOrderItems() { return orderItems; }
    public String getOrderInfo() { return orderInfo; }
    public String getOrderId() { return orderId; }
    public String getAnnouncement() { return announcement; }
    public LocalDateTime getDeadline() { return deadline; }
}