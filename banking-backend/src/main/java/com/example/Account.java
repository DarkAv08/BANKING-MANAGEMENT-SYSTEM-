package com.example;

// Using a record for a clean, immutable data model (Java 17 feature)
public record Account(int id, String name, double balance) {}