package com.jpmc.midascore.foundation;

public class IncentiveRequest {
    private long sender;
    private long recipient;
    private float amount;

    public IncentiveRequest() {}

    public IncentiveRequest(long sender, long recipient, float amount) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
    }

    public long getSender() { return sender; }
    public void setSender(long sender) { this.sender = sender; }

    public long getRecipient() { return recipient; }
    public void setRecipient(long recipient) { this.recipient = recipient; }

    public float getAmount() { return amount; }
    public void setAmount(float amount) { this.amount = amount; }
}
