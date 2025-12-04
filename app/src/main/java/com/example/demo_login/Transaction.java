package com.example.demo_login;

public class Transaction {
    private int id;
    private double amount;
    private String categoryName;
    private String description; // Note
    private String date;
    private String type; // "income" or "expense"

    // Constructor
    public Transaction(int id, double amount, String categoryName, String description, String date, String type) {
        this.id = id;
        this.amount = amount;
        this.categoryName = categoryName;
        this.description = description;
        this.date = date;
        this.type = type;
    }
    public int getId() {
        return id;
    }

    public double getAmount() { return amount; }
    public String getCategoryName() { return categoryName; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getType() { return type; }
}