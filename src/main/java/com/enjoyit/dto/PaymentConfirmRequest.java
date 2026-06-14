package com.enjoyit.dto;
public class PaymentConfirmRequest {
    private String participantId;
    private int amount;
    // Getters & Setters
    public String getParticipantId() { return participantId; }
    public void setParticipantId(String participantId) { this.participantId = participantId; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
}