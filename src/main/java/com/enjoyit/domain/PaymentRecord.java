package com.enjoyit.domain;

public class PaymentRecord {
    private String participantId;
    private int amountDue;
    private String status; // "未付款" 或 "已付款"
    private String remarks; // 【新增】備註欄位，用來記錄多收找零或尚欠金額

    public PaymentRecord() {}

    public PaymentRecord(String participantId, int amountDue) {
        this.participantId = participantId;
        this.amountDue = amountDue;
        this.status = "未付款";
        this.remarks = ""; // 預設為空字串
    }

    public void markAsPaid() { this.status = "已付款"; }
    public void markAsUnpaid() { this.status = "未付款"; }

    // Getters and Setters
    public String getParticipantId() { return participantId; }
    public void setParticipantId(String participantId) { this.participantId = participantId; }
    public int getAmountDue() { return amountDue; }
    public void setAmountDue(int amountDue) { this.amountDue = amountDue; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}