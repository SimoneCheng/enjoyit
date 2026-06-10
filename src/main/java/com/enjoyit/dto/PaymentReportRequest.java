package com.enjoyit.dto;
public class PaymentReportRequest {
    private String participantId;
    private String method;
    private String details;
    // Getters & Setters
    public String getParticipantId() { return participantId; }
    public void setParticipantId(String participantId) { this.participantId = participantId; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}