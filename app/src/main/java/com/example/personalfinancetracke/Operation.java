package com.example.personalfinancetracke;

public class Operation {
    private int id;
    private String type;
    private double amount;
    private String category;
    private String date;
    
    public Operation(int id, String type, double amount, String category, String date) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }
    
    public int getId() { return id; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getDate() { return date; }
}
