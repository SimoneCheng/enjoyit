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

    public GroupOrder(String info) {
        this.status = "進行中";
        // 模擬密碼生成邏輯
        this.adminPassword = "pwd" + String.valueOf(System.currentTimeMillis()).substring(8);
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

    // Getters 供 Controller 或聚合器使用
    public String getStatus() { return status; }
    public String getAdminPassword() { return adminPassword; }
    public List<OrderItem> getOrderItems() { return orderItems; }
}