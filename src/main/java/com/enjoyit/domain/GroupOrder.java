package com.enjoyit.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupOrder {
    private String status;
    private LocalDateTime deadline;
    private String adminPassword;
    private String announcement;
    private List<OrderItem> orderItems = new ArrayList<>();
    private String orderId;
    private String orderInfo;
    private String vendorId; // 綁定店家的 ID
    private String groupId;  // 綁定群組帳號

    // === UC-07 最新財務管理屬性 ===
    private List<PaymentRecord> paymentRecords = new ArrayList<>();

    public GroupOrder(String info) {
        this.orderInfo = info;
        this.status = "進行中";
    }

    // --- 原有基礎屬性的 Getters & Setters ---
    public String getVendorId() { return vendorId; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }
    public String getAdminPassword() { return adminPassword; }

    public void setOrderDeadline(LocalDateTime newTime) {
        this.deadline = newTime;
        if (newTime != null && newTime.isBefore(LocalDateTime.now())) {
            this.status = "已結單";
        } else {
            this.status = "進行中";
        }
    }
    public LocalDateTime getOrderDeadline() { return deadline; }

    public void setAnnouncement(String announcement) { this.announcement = announcement; }
    public String getAnnouncement() { return announcement; }

    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }

    public void setOrderInfo(String orderInfo) { this.orderInfo = orderInfo; }
    public String getOrderInfo() { return orderInfo; }

    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getOrderId() { return orderId; }

    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }

    // ==========================================================
    // ===          UC-07 財務管理最新修正業務邏輯             ===
    // ==========================================================

    /**
     * 動態計算最新點餐品項總額 (改為依據 orderFor 訂購人姓名來分群)
     */
    public List<Map<String, Object>> getFinanceSummary() {
        Map<String, Integer> totalMap = new HashMap<>();
        Map<String, List<String>> itemsMap = new HashMap<>();

        // 1. 遍歷訂單，按「實際訂購人姓名」分類
        for (OrderItem item : orderItems) {
            // 如果沒填名字，統一歸類為 "未命名"
            String payer = (item.getOrderFor() == null || item.getOrderFor().trim().isEmpty())
                    ? "未命名" : item.getOrderFor().trim();

            totalMap.put(payer, totalMap.getOrDefault(payer, 0) + item.getOrderTotalPrice());

            String itemDesc = item.getItemName() + " x" + item.getQuantity();
            if (item.getCustomizations() != null && !item.getCustomizations().isEmpty()) {
                itemDesc += " " + item.getCustomizations().toString();
            }
            itemsMap.computeIfAbsent(payer, k -> new ArrayList<>()).add(itemDesc);
        }

        // 2. 更新財務紀錄狀態
        List<PaymentRecord> updatedRecords = new ArrayList<>();
        for (String payer : totalMap.keySet()) {
            final String currentPayer = payer;
            PaymentRecord existing = this.paymentRecords.stream()
                    .filter(r -> r.getPayerName().equals(currentPayer))
                    .findFirst()
                    .orElse(null);

            if (existing == null) {
                existing = new PaymentRecord(payer, totalMap.get(payer));
            } else {
                existing.setAmountDue(totalMap.get(payer));
            }
            updatedRecords.add(existing);
        }
        this.paymentRecords = updatedRecords;

        // 3. 包裝給前端
        List<Map<String, Object>> summaryList = new ArrayList<>();
        for (PaymentRecord record : this.paymentRecords) {
            Map<String, Object> map = new HashMap<>();
            map.put("payerName", record.getPayerName());
            map.put("amountDue", record.getAmountDue());
            map.put("status", record.getStatus());
            map.put("remarks", record.getRemarks());
            map.put("details", itemsMap.getOrDefault(record.getPayerName(), new ArrayList<>()));
            summaryList.add(map);
        }
        return summaryList;
    }

    /**
     * 更新指定訂購人的付款狀態
     */
    public void updatePaymentStatus(String payerName, String status, String remarks) {
        final String currentPayer = payerName;
        PaymentRecord record = this.paymentRecords.stream()
                .filter(r -> r.getPayerName().equals(currentPayer))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("找不到該訂購人的帳單項目"));

        if ("已付款".equals(status)) {
            record.markAsPaid();
        } else {
            record.markAsUnpaid();
        }
        record.setRemarks(remarks);
    }

    // 財務紀錄清單的 Getter & Setter
    public List<PaymentRecord> getPaymentRecords() { return paymentRecords; }
    public void setPaymentRecords(List<PaymentRecord> paymentRecords) { this.paymentRecords = paymentRecords; }
}