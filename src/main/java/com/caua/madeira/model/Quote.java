package com.caua.madeira.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Quote {
    private int id;
    private String name;
    private int clientId;
    private String clientName;
    private LocalDate date;
    private double shippingValue;
    private double totalValue;
    private List<QuoteItem> items;
    
    public Quote() {
        this.date = LocalDate.now();
    }
    
    public Quote(int id, String name, int clientId, String clientName, LocalDate date, 
                double shippingValue, double totalValue, List<QuoteItem> items) {
        this.id = id;
        this.name = name;
        this.clientId = clientId;
        this.clientName = clientName;
        this.date = date != null ? date : LocalDate.now();
        this.shippingValue = shippingValue;
        this.totalValue = totalValue;
        this.items = items;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getClientId() {
        return clientId;
    }
    
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }
    
    public String getClientName() {
        return clientName;
    }
    
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public String getFormattedDate() {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public double getShippingValue() {
        return shippingValue;
    }
    
    public void setShippingValue(double shippingValue) {
        this.shippingValue = shippingValue;
    }
    
    public double getTotalValue() {
        return totalValue;
    }
    
    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }
    
    public List<QuoteItem> getItems() {
        return items;
    }
    
    public void setItems(List<QuoteItem> items) {
        this.items = items;
        calculateTotal();
    }
    
    public void addItem(QuoteItem item) {
        this.items.add(item);
        calculateTotal();
    }
    
    public void removeItem(QuoteItem item) {
        this.items.remove(item);
        calculateTotal();
    }
    
    private void calculateTotal() {
        if (items != null) {
            this.totalValue = items.stream()
                .mapToDouble(QuoteItem::getTotal)
                .sum() + shippingValue;
        } else {
            this.totalValue = shippingValue;
        }
    }
}
