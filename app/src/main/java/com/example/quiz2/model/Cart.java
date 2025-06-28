package com.example.quiz2.model;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private static Cart instance;
    private final List<CartItem> items;

    private Cart() {
        items = new ArrayList<>();
    }

    public static Cart getInstance() {
        if (instance == null) {
            instance = new Cart();
        }
        return instance;
    }

    public void addItem(String name, double price, int quantity, String type) {
        for (CartItem item : items) {
            if (item.getName().equals(name) && item.getType().equals(type)) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        items.add(new CartItem(name, price, quantity, type));
    }

    public void removeItem(String name, String type) {
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            if (item.getName().equals(name) && item.getType().equals(type)) {
                items.remove(i);
                break;
            }
        }
    }

    public List<CartItem> getItems() {
        return items;
    }

    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : items) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    public void clear() {
        items.clear();
    }
}
