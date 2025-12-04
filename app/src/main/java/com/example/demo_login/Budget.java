package com.example.demo_login;

public class Budget {
    private int id;
    private String category;
    private double maxAmount;
    private int month;
    private int year;

    public Budget(int id, String category, double maxAmount, int month, int year) {
        this.id = id;
        this.category = category;
        this.maxAmount = maxAmount;
        this.month = month;
        this.year = year;
    }

    public int getId() { return id; }
    public String getCategory() { return category; }
    public double getMaxAmount() { return maxAmount; }
    public int getMonth() { return month; }
    public int getYear() { return year; }
}