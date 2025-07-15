// src/main/java/com/converter/model/Currency.java
package com.converter.model;

public class Currency {
    private String code;
    private String name;
    private double exchangeRate; // Taux par rapport à l'EUR (base)
    
    // Constructeurs
    public Currency() {}
    
    public Currency(String code, String name, double exchangeRate) {
        this.code = code;
        this.name = name;
        this.exchangeRate = exchangeRate;
    }
    
    // Getters et Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public double getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(double exchangeRate) { this.exchangeRate = exchangeRate; }
    
    @Override
    public String toString() {
        return "Currency{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", exchangeRate=" + exchangeRate +
                '}';
    }
}