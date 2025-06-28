package com.example.quiz2.model;

public class CartItem {
    private String name;
    private double price;
    private int quantity;
    private String type; // "food" or "drink"

    public CartItem(String name, double price, int quantity, String type) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.type = type;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getType() { return type; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
}

