package com.example.finance.core.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Operation {
    private String id = UUID.randomUUID().toString();
    private OperationType type;
    private String category;
    private double amount;
    private LocalDateTime createdAt = LocalDateTime.now();
    private String note;

    public Operation() {}

    public Operation(OperationType type, String category, double amount, String note) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.note = note;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public OperationType getType() { return type; }
    public void setType(OperationType type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
