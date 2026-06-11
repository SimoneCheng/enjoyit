package com.enjoyit.domain;

public class PaymentRecord {
    private String payerName; // 【修改】改為依據訂購人姓名
    private int amountDue;
    private String status;
    private String remarks;

    public PaymentRecord() {}

    public PaymentRecord(String payerName, int amountDue) {
        this.payerName = payerName;
        this.amountDue = amountDue;
        this.status = "未付款";
        this.remarks = "";
    }

    public void markAsPaid() { this.status = "已付款"; }
    public void markAsUnpaid() { this.status = "未付款"; }

    // Getters and Setters
    public String getPayerName() { return payerName; }
    public void setPayerName(String payerName) { this.payerName = payerName; }
    public int getAmountDue() { return amountDue; }
    public void setAmountDue(int amountDue) { this.amountDue = amountDue; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}