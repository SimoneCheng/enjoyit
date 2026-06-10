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
     * 功能 1 核心邏輯：動態計算最新點餐品項總額，並回傳主揪對帳表專用的資料結構
     */
    public List<Map<String, Object>> getFinanceSummary() {
        Map<String, Integer> totalMap = new HashMap<>();
        Map<String, List<String>> itemsMap = new HashMap<>();

        // 1. 遍歷當前訂單內的所有餐點品項，按參與者裝置(participantId)進行分類與金額累加
        for (OrderItem item : orderItems) {
            String pId = item.getParticipantId();
            if (pId == null) continue;

            // 累加該裝置的應付總金額
            totalMap.put(pId, totalMap.getOrDefault(pId, 0) + item.getOrderTotalPrice());

            // 組合餐點明細字串：例如 "王小明: 珍珠奶茶 x1 [半糖, 去冰]"
            String itemDesc = item.getOrderFor() + ": " + item.getItemName() + " x" + item.getQuantity();
            if (item.getCustomizations() != null && !item.getCustomizations().isEmpty()) {
                itemDesc += " " + item.getCustomizations().toString();
            }
            itemsMap.computeIfAbsent(pId, k -> new ArrayList<>()).add(itemDesc);
        }

        // 2. 同步維護內部的 paymentRecords 狀態，確保主揪勾選的「已付款」狀態不會因為使用者加點刷新而消失
        List<PaymentRecord> updatedRecords = new ArrayList<>();
        for (String pId : totalMap.keySet()) {
            final String currentPid = pId;
            PaymentRecord existing = this.paymentRecords.stream()
                    .filter(r -> r.getParticipantId().equals(currentPid))
                    .findFirst()
                    .orElse(null);

            if (existing == null) {
                // 如果是第一次點餐的裝置，建立全新的財務紀錄（預設未付款）
                existing = new PaymentRecord(pId, totalMap.get(pId));
            } else {
                // 如果是已有紀錄的裝置，僅更新其最新應付金額
                existing.setAmountDue(totalMap.get(pId));
            }
            updatedRecords.add(existing);
        }
        this.paymentRecords = updatedRecords;

        // 3. 包裝成前端直觀對帳表格所需的結構 (List<Map>)
        List<Map<String, Object>> summaryList = new ArrayList<>();
        for (PaymentRecord record : this.paymentRecords) {
            Map<String, Object> map = new HashMap<>();
            map.put("participantId", record.getParticipantId());
            map.put("amountDue", record.getAmountDue());
            map.put("status", record.getStatus());
            map.put("remarks", record.getRemarks()); // 【新增這行】將備註傳給前端
            map.put("details", itemsMap.getOrDefault(record.getParticipantId(), new ArrayList<>()));
            summaryList.add(map);
        }
        return summaryList;
    }

    /**
     * 功能 1 核心邏輯：由主揪在表格中點擊按鈕，一鍵切換變更付款狀態
     */
//    public void updatePaymentStatus(String participantId, String status) {
//        final String currentPid = participantId;
//        PaymentRecord record = this.paymentRecords.stream()
//                .filter(r -> r.getParticipantId().equals(currentPid))
//                .findFirst()
//                .orElseThrow(() -> new IllegalArgumentException("找不到該參與者的帳單項目"));
//
//        if ("已付款".equals(status)) {
//            record.markAsPaid();
//        } else {
//            record.markAsUnpaid();
//        }
//    }
    // 【修改】多接收一個 remarks 參數
    public void updatePaymentStatus(String participantId, String status, String remarks) {
        final String currentPid = participantId;
        PaymentRecord record = this.paymentRecords.stream()
                .filter(r -> r.getParticipantId().equals(currentPid))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("找不到該參與者的帳單項目"));

        if ("已付款".equals(status)) {
            record.markAsPaid();
        } else {
            record.markAsUnpaid();
        }
        record.setRemarks(remarks); // 【新增】同時更新備註
    }

    /**
     * 功能 2 核心邏輯：供一般使用者裝置(個人網頁)即時查詢自己這台裝置當前的應付總額與核帳狀態
     */
    public Map<String, Object> getSingleParticipantStatus(String participantId) {
        getFinanceSummary(); // 每次查詢前強制刷新對帳單，確保拿到最即時的加點金額
        final String currentPid = participantId;
        PaymentRecord record = this.paymentRecords.stream()
                .filter(r -> r.getParticipantId().equals(currentPid))
                .findFirst()
                .orElse(null);

        Map<String, Object> result = new HashMap<>();
        if (record != null) {
            result.put("amountDue", record.getAmountDue());
            result.put("status", record.getStatus());
        } else {
            result.put("amountDue", 0);
            result.put("status", "無點餐紀錄");
        }
        return result;
    }

    // 財務紀錄清單的 Getter & Setter
    public List<PaymentRecord> getPaymentRecords() { return paymentRecords; }
    public void setPaymentRecords(List<PaymentRecord> paymentRecords) { this.paymentRecords = paymentRecords; }
}