package com.caua.madeira.model;

public class QuoteItem {
    private String id;
    private int quantity;
    private double width;
    private double height;
    private double length;
    private double unitValue;
    private double total;
    
    public QuoteItem() {
        this.quantity = 0;
        this.width = 0.0;
        this.height = 0.0;
        this.length = 0.0;
        this.unitValue = 0.0;
        this.total = 0.0;
    }
    
    public QuoteItem(String id, int quantity, double width, double height, double length, double unitValue) {
        this.id = id;
        this.quantity = quantity;
        this.width = width;
        this.height = height;
        this.length = length;
        this.unitValue = unitValue;
        calculateTotal();
    }
    
    public void calculateTotal() {
        // Total baseado no novo cálculo de M3: quantidade * largura(cm) * altura(cm) / 10000
        // Ou seja, M3 já considera a quantidade, portanto não multiplicar novamente por quantity aqui
        double cubicMeters = getCubicMeters();
        this.total = cubicMeters * unitValue;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateTotal();
    }
    
    public double getWidth() {
        return width;
    }
    
    public void setWidth(double width) {
        this.width = width;
        calculateTotal();
    }
    
    public double getHeight() {
        return height;
    }
    
    public void setHeight(double height) {
        this.height = height;
        calculateTotal();
    }
    
    public double getLength() {
        return length;
    }
    
    public void setLength(double length) {
        this.length = length;
        calculateTotal();
    }
    
    public double getUnitValue() {
        return unitValue;
    }
    
    public void setUnitValue(double unitValue) {
        this.unitValue = unitValue;
        calculateTotal();
    }
    
    public double getTotal() {
        return total;
    }
    
    // Calculate cubic meters
    public double getCubicMeters() {
        // Novo cálculo: quantidade * largura(cm) * altura(cm) / 10000
        // Resultado em m³ considerando largura e altura em cm e sem considerar o comprimento
        return (quantity * width * height * length) / 10000.0;
    }

    /**
     * Formata um valor double para string com vírgula como separador decimal.
     */
    public static String formatDoubleBr(double value) {
        return String.format("%.3f", value).replace('.', ',');
    }
}
