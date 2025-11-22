package com.gutierrez_rodriguez.burritosmedallas;

public class Product {

    // Los nombres de estas variables DEBEN ser idénticos a los campos en Firestore
    private String name;
    private String description;
    private double price; // Usamos double para números con decimales
    private String category;
    private boolean available;

    // --- IMPORTANTE: Constructor vacío ---
    // Firebase necesita esto forzosamente para convertir el documento en este objeto.
    public Product() {
    }

    // Constructor completo (opcional, útil para pruebas)
    public Product(String name, String description, double price, String category, boolean available) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.available = available;
    }

    // --- Getters ---
    // Necesarios para que el código pueda leer la info del producto
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public boolean isAvailable() {
        return available;
    }
}