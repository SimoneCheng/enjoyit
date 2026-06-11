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
    private String vendorId;
    private String groupId;
    private List<PaymentRecord> paymentRecords = new ArrayList<>();

    public GroupOrder(String info) {
        this.orderInfo = info;
        this.status = "進行中";
    }

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

    public LocalDateTime getDeadline() { return deadline; }

    public void setAnnouncement(String announcement) { this.announcement = announcement; }

    public String getAnnouncement() { return announcement; }

    public void setStatus(String status) { this.status = status; }

    public String getStatus() {
        if ("進行中".equals(status) && deadline != null && LocalDateTime.now().isAfter(deadline)) {
            return "已結單";
        }
        return status;
    }

    public void setOrderInfo(String orderInfo) { this.orderInfo = orderInfo; }

    public String getOrderInfo() { return orderInfo; }

    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getOrderId() { return orderId; }

    public List<OrderItem> getOrderItems() { return orderItems; }

    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }

    public List<Map<String, Object>> getFinanceSummary() {
        Map<String, Integer> totalMap = new HashMap<>();
        Map<String, List<String>> itemsMap = new HashMap<>();

        for (OrderItem item : orderItems) {
            String payer = (item.getOrderFor() == null || item.getOrderFor().trim().isEmpty())
                    ? "未命名"
                    : item.getOrderFor().trim();

            totalMap.put(payer, totalMap.getOrDefault(payer, 0) + item.getOrderTotalPrice());

            String itemDesc = item.getItemName() + " x" + item.getQuantity();
            if (item.getCustomizations() != null && !item.getCustomizations().isEmpty()) {
                itemDesc += " " + item.getCustomizations();
            }
            itemsMap.computeIfAbsent(payer, key -> new ArrayList<>()).add(itemDesc);
        }

        List<PaymentRecord> updatedRecords = new ArrayList<>();
        for (String payer : totalMap.keySet()) {
            PaymentRecord existing = this.paymentRecords.stream()
                    .filter(record -> record.getPayerName().equals(payer))
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

    public void updatePaymentStatus(String payerName, String status, String remarks) {
        PaymentRecord record = this.paymentRecords.stream()
                .filter(item -> item.getPayerName().equals(payerName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("找不到該訂購人的帳單項目"));

        if ("已付款".equals(status)) {
            record.markAsPaid();
        } else {
            record.markAsUnpaid();
        }
        record.setRemarks(remarks);
    }

    public List<PaymentRecord> getPaymentRecords() { return paymentRecords; }

    public void setPaymentRecords(List<PaymentRecord> paymentRecords) { this.paymentRecords = paymentRecords; }
}
