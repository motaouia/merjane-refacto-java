package com.nimbleways.springboilerplate.entities;

public enum ProductType {
    NORMAL,
    SEASONAL,
    EXPIRABLE;
    
    public static ProductType from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Product type is null");
        }
        try {
            return ProductType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown product type: " + value, ex);
        }
    }
}