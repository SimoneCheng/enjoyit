package com.enjoyit.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GroupOrder {
    private String status;
    private LocalDateTime deadline;
    private String adminPassword;
    private String announcement;
    private List<OrderItem> orderItems = new ArrayList<>();
    private String orderId;
    private String orderInfo;
    private String vendorId; // 【新增】綁定店家的 ID
    private String groupId; // 綁定群組帳號

    public GroupOrder(String info) {
        this.orderInfo = info;
        this.status = "進行中";
    }

    // --- 新增與修改的 Getters & Setters ---
    public String getVendorId() { return vendorId; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }
    public String getAdminPassword() { return adminPassword; }

    public void setOrderDeadline(LocalDateTime newTime) {
        this.deadline = newTime;
        if (newTime.isBefore(LocalDateTime.now())) {
            this.status = "已結單";
        }
    }

    public void setAnnouncement(String announcement) { this.announcement = announcement; }
    public void setStatus(String status) { this.status = status; }
    public void setOrderInfo(String orderInfo) { this.orderInfo = orderInfo; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getStatus() { return status; }
    public List<OrderItem> getOrderItems() { return orderItems; }
    public String getOrderInfo() { return orderInfo; }
    public String getOrderId() { return orderId; }
    public String getAnnouncement() { return announcement; }
    public LocalDateTime getDeadline() { return deadline; }
}