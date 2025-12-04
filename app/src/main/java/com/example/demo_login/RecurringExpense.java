package com.example.demo_login;

public class RecurringExpense {
    private int userId;
    private int id;
    private String category;
    private double amount;
    private String frequency;
    private String startDate;
    private String endDate;
    private String status;

    public RecurringExpense(int id, int userId, String category, double amount, String frequency, String startDate, String endDate, String status) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.amount = amount;
        this.frequency = frequency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }
    public int getUserId() { return userId; }

    public int getId() { return id; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public String getFrequency() { return frequency; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getStatus() { return status; }
}