package com.enjoyit.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MenuCategory {
    private String id;
    private String name;
    private List<MenuItem> items;
    @JsonProperty("isActive")
    private boolean isActive = true; // 新增這行

    public MenuCategory(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.items = new ArrayList<>();
    }

    public void addItem(MenuItem item) {
        this.items.add(item);
    }

    public MenuCategory() {
        this.id = java.util.UUID.randomUUID().toString();
        this.items = new java.util.ArrayList<>(); }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<MenuItem> getItems() { return items; }
}