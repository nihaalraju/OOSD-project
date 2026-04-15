package com.supportticket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Ticket {
    private String ticketId;
    private String customerName;
    private String email;
    private String category;
    private String priority;
    private String description;
    private String status;
    private String createdDate;
    private String resolvedDate;

    public Ticket(String customerName, String email, String category, String priority, String description) {
        this.ticketId = "TKT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.customerName = customerName;
        this.email = email;
        this.category = category;
        this.priority = priority;
        this.description = description;
        this.status = "Open";
        this.createdDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.resolvedDate = "-";
    }

    // Getters and Setters
    public String getTicketId() { return ticketId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String name) { this.customerName = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getDescription() { return description; }
    public void setDescription(String desc) { this.description = desc; }
    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        if (status.equals("Resolved") || status.equals("Closed")) {
            this.resolvedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
    }
    public String getCreatedDate() { return createdDate; }
    public String getResolvedDate() { return resolvedDate; }

    @Override
    public String toString() {
        return ticketId + " | " + customerName + " | " + category + " | " + priority + " | " + status;
    }
}
